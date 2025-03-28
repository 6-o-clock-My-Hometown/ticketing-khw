package com.example.sparta_ticketing.reserve;

import com.example.sparta_ticketing.domain.auth.entity.AuthUser;
import com.example.sparta_ticketing.domain.reserve.dto.request.ReserveRequest;
import com.example.sparta_ticketing.domain.reserve.repository.ReserveRepository;
import com.example.sparta_ticketing.domain.reserve.service.ReserveService;
import com.example.sparta_ticketing.domain.seat.enums.SeatEnum;
import com.example.sparta_ticketing.domain.seat.repository.SeatRepository;
import com.example.sparta_ticketing.domain.show.dto.request.CreateShowRequestDto;
import com.example.sparta_ticketing.domain.show.dto.request.CreateShowSeatsRequestDto;
import com.example.sparta_ticketing.domain.show.entity.Show;
import com.example.sparta_ticketing.domain.show.enums.Category;
import com.example.sparta_ticketing.domain.show.enums.Region;
import com.example.sparta_ticketing.domain.show.repository.ShowRepository;
import com.example.sparta_ticketing.domain.show.service.ShowService;
import com.example.sparta_ticketing.domain.user.entity.User;
import com.example.sparta_ticketing.domain.user.enums.UserRole;
import com.example.sparta_ticketing.domain.user.repository.UserBulkRepository;
import com.example.sparta_ticketing.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReserveTest {
    @Autowired
    private ReserveService reserveService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBulkRepository userBulkRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReserveRepository reserveRepository;

    @Autowired
    private ShowService showService;

    private Show show;

    @BeforeAll
    void setup() throws InterruptedException {
        // 1. 유저 100명 생성
        List<User> users = IntStream.range(0, 100)
                .mapToObj(i -> new User("user" + i + "@test.com", "testtest", "nick" + i, "010-0000-0000", "2025-01-01", UserRole.ROLE_USER))
                .toList();
        userBulkRepository.bulkInsert(users);

        // 2. 디렉터 생성
        User director = userRepository.findAll().get(0);
        AuthUser authUser = new AuthUser(director.getId(), director.getEmail(), director.getUserRole());

        // 3. 공연 생성 요청 DTO 만들기
        CreateShowRequestDto createShowRequestDto = new CreateShowRequestDto(
                "테스트 공연",
                Category.CONCERT,
                "공연 설명입니다.",
                Region.SEOUL,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusSeconds(1), // 1초 뒤 예매 시작
                LocalDateTime.now().plusDays(1),    // 예매 종료는 1일 후
                List.of(
                        new CreateShowSeatsRequestDto(SeatEnum.VIP, 10, 100000)
                )
        );

        // 4. 서비스로 생성 호출 (→ 이 안에서 Redis까지 세팅됨)
        showService.createShow(authUser, createShowRequestDto);

        Thread.sleep(2000);
        // 5. 생성된 공연 조회
        List<Show> shows = showRepository.findAll();
        show = shows.get(shows.size() - 1);
    }
    @Test
    void concurrentReserveTest() throws InterruptedException {
        int executeCount = 100; // 동시에 요청이 들어오는 예매 요청 수
        ExecutorService executor = Executors.newFixedThreadPool(20); //동시에 최대 32개의 스레드 가동
        CyclicBarrier barrier = new CyclicBarrier(20); //모든 스레드가 준비될 때까지 대기하게 해주는 장치
        CountDownLatch latch = new CountDownLatch(100); // 모든 작업이 끝날 때까지 메인 스레드를 대기 시킴

        List<User> users = userRepository.findAll();
        ReserveRequest request = new ReserveRequest(show.getId(), SeatEnum.VIP);

        for (int i = 0; i < executeCount; i++) {
            final int idx = i;
            executor.execute(() -> {
                try {
                    System.out.println("모든 스레드 준비될 때까지 대기 전");
                    barrier.await(); // 모든 스레드 준비될 때까지 대기
                    System.out.println("모든 스레드 준비될 때까지 대기 후");
                    AuthUser authUser = new AuthUser(users.get(idx).getId(), users.get(idx).getEmail(), UserRole.ROLE_USER);

                    reserveService.reserve(authUser, request);
                } catch (Exception e) {
                    System.out.println("예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown(); // 이게 요청예매 수 만큼 불리면 latch.await이 풀림.
                }
            });
        }
        System.out.println("모든 스레드 끝날 때까지 대기 전");
        latch.await();
        System.out.println("모든 스레드 끝날 때까지 대기 후");

        long reserveCount = reserveRepository.count();
        System.out.println("최종 예약된 수: " + reserveCount);

        // 좌석은 10개였으므로, 최대 10개만 예약되어야 한다
        Assertions.assertThat(reserveCount).isEqualTo(10);
    }
}
