package com.example.sparta_ticketing.show.viewCount;

import com.example.sparta_ticketing.common.redis.ViewCountService;
import com.example.sparta_ticketing.domain.show.entity.Show;
//import com.example.sparta_ticketing.domain.show.entity.ViewCount;
import com.example.sparta_ticketing.domain.show.enums.Category;
import com.example.sparta_ticketing.domain.show.enums.Region;
import com.example.sparta_ticketing.domain.show.repository.ShowRepository;
//import com.example.sparta_ticketing.domain.show.repository.ViewCountRepository;
import com.example.sparta_ticketing.domain.user.entity.User;
import com.example.sparta_ticketing.domain.user.enums.UserRole;
import com.example.sparta_ticketing.domain.user.repository.UserBulkRepository;
import com.example.sparta_ticketing.domain.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ViewCountPerformanceTest {

    private final ShowRepository showRepository;
    private final UserBulkRepository userBulkRepository;
    private final UserRepository userRepository;
//    private final ViewCountRepository viewCountRepository;
    private final ViewCountService viewCountService;

    @Autowired
    public ViewCountPerformanceTest(ShowRepository showRepository,
                                      UserBulkRepository userBulkRepository,
                                      UserRepository userRepository,
//                                      ViewCountRepository viewCountRepository,
                                    ViewCountService viewCountService) {
        this.showRepository = showRepository;
        this.userBulkRepository = userBulkRepository;
        this.userRepository = userRepository;
//        this.viewCountRepository = viewCountRepository;
        this.viewCountService = viewCountService;
    }

//    @Test
//    @Transactional
//    @Rollback(false)
//    public void DB_공연조회_성능테스트() {
//        // 1. 게시글 1개 생성
//        User director = userRepository.save(new User(
//                "director@test.com", "password", "director", "010-0000-0000", "1990-01-01", UserRole.ROLE_DIRECTOR
//        ));
//
//        Show show = showRepository.save(new Show(
//                "테스트 공연",
//                Category.CONCERT,
//                "설명",
//                Region.SEOUL,
//                LocalDateTime.now().plusDays(1),
//                LocalDateTime.now().plusDays(2),
//                LocalDateTime.now(),
//                LocalDateTime.now().plusDays(1),
//                100,
//                director
//        ));
//
//        // 2. 유저 생성 및 벌크 인서트
//        List<User> users = new ArrayList<>();
//        for (int i = 0; i <= 100; i++) {
//            users.add(new User(
//                    "user" + i + "@test.com",
//                    "password",
//                    "nickname" + i,
//                    "010-1234-5678",
//                    "2025-01-01",
//                    UserRole.ROLE_USER
//            ));
//        }
//        userBulkRepository.bulkInsert(users);
//
//        // 3. 새로 저장된 유저들 조회
//        List<Long> userIds = userRepository.findAll().stream()
//                .map(User::getId)
//                .toList();
//
//        // 4. 시간 측정 시작
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//
//        // 5. 유저 게시글 조회
//        for (Long userId : userIds) {
//            User user = userRepository.findById(userId).orElseThrow();
//            try {
//                viewCountRepository.save(new ViewCount(user, show));
//            } catch (DataIntegrityViolationException e) {
//                // 중복 무시
//            }
//
//            viewCountRepository.countByShow(show); // 단순 조회
//        }
//
//        // 6. 시간 측정 종료
//        stopWatch.stop();
//        System.out.println("DB 방식 조회 소요 시간(ms): " + stopWatch.getTotalTimeMillis());
//    }


    @Test
    @Transactional
    @Rollback(false)
    public void Redis_공연조회_성능테스트() {
        // 1. 게시글 1개 생성
        User director = userRepository.save(new User(
                "director2@test.com", "password", "director", "010-0000-0000", "2025-01-01", UserRole.ROLE_DIRECTOR
        ));

        Show show = showRepository.save(new Show(
                "테스트 공연",
                Category.CONCERT,
                "설명",
                Region.SEOUL,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                100,
                director
        ));

        // 2. 유저 생성 및 벌크 인서트
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            users.add(new User(
                    "redis_user" + i + "@test.com",
                    "password",
                    "nickname" + i,
                    "010-1234-5678",
                    "2025-01-01",
                    UserRole.ROLE_USER
            ));
        }
        userBulkRepository.bulkInsert(users);

        // 3. 새로 저장된 유저들 조회
        List<Long> userIds = userRepository.findAll().stream()
                .map(User::getId)
                .toList();

        // 4. 시간 측정 시작
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 5. 유저 1000명이 Redis로 조회수 증가
        for (Long userId : userIds) {
            viewCountService.increaseViewCount(show.getId(), userId);
            viewCountService.getViewCount(show.getId()); // 단순 조회
        }

        // 6. 시간 측정 종료
        stopWatch.stop();
        System.out.println("Redis 방식 조회 소요 시간(ms): " + stopWatch.getTotalTimeMillis());
    }
}