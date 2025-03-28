package com.example.sparta_ticketing.domain.show.service;

import com.example.sparta_ticketing.common.aop.ViewCount;
import com.example.sparta_ticketing.common.exception.InvalidRequestException;
import com.example.sparta_ticketing.common.exception.ShowNotFoundException;
import com.example.sparta_ticketing.common.redis.ViewCountService;
import com.example.sparta_ticketing.domain.auth.entity.AuthUser;
import com.example.sparta_ticketing.domain.seat.entity.Seat;
import com.example.sparta_ticketing.domain.seat.repository.SeatRepository;
import com.example.sparta_ticketing.domain.show.dto.request.CreateShowRequestDto;
import com.example.sparta_ticketing.domain.show.dto.request.CreateShowSeatsRequestDto;
import com.example.sparta_ticketing.domain.show.dto.request.UpdateShowRequestDto;
import com.example.sparta_ticketing.domain.show.dto.response.PagingShowResponse;
import com.example.sparta_ticketing.domain.show.dto.response.ShowResponseDto;
import com.example.sparta_ticketing.domain.show.entity.Show;
import com.example.sparta_ticketing.domain.show.enums.ShowStatus;
import com.example.sparta_ticketing.domain.show.repository.ShowRepository;
import com.example.sparta_ticketing.domain.user.entity.User;
import com.example.sparta_ticketing.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ShowService {
    private final ShowRepository showRepository;
    private final UserService userService;
    private final SeatRepository seatRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ViewCountService viewCountService;

    @Transactional
    public void createShow(AuthUser authUser, CreateShowRequestDto createShowRequestDto) {
        User user = userService.findById(authUser.getId()).orElseThrow(()-> new EntityNotFoundException("회원을 찾지 못했습니다."));

        int totalSeats = 0;
        for (CreateShowSeatsRequestDto seat: createShowRequestDto.getSeats()) {
            totalSeats += seat.getSeatCount();
        }
        if(totalSeats == 0){
            throw new InvalidRequestException("좌석의 총 개수가 0이 될 수 없습니다.");
        }

        Show show = new Show(createShowRequestDto, totalSeats, user);

        Show savedShow = showRepository.save(show);

        List<Seat> seats = createShowRequestDto.getSeats().stream()
                .map(dto -> new Seat(savedShow, dto.getSeatName(), dto.getSeatCount(), dto.getSeatPrice()))
                .collect(Collectors.toList());

        String redisReserveKey = "reserve:start:" + show.getId();
        LocalDateTime now = LocalDateTime.now();
        try{
            redisTemplate.opsForValue().set(redisReserveKey, "trigger", Duration.between(now, show.getReservationStartDate()));
        } catch (Exception e){
            throw new InvalidRequestException("예매 시작시간을 현재 시간보다 늦게 설정해주세요.");
        }

        seatRepository.saveAll(seats);
    }

    @Transactional(readOnly = true)
    public PagingShowResponse getShowList(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Show> showPage = showRepository.findByStatus(ShowStatus.NOT_DELETED, pageable);
        List<ShowResponseDto> shows = showPage.getContent()
                .stream()
                .map(ShowResponseDto::toDtos)
                .toList();
        return new PagingShowResponse(
                shows,
                showPage.getNumber(),
                showPage.getSize(),
                showPage.getTotalPages(),
                showPage.getTotalElements()
        );
    }

    /**
     * 특정 공연 조회
     *
     * @param showId (조회할 공연 Id)
     * @return Show
     */
    @ViewCount
    @Transactional
    public ShowResponseDto getShow(Long showId, AuthUser authUser) {
        Show show = findShow(showId);

        viewCountService.increaseViewCount(show.getId(), authUser.getId());
        long viewCount = viewCountService.getViewCount(show.getId());

        return ShowResponseDto.toDto(show, viewCount);
    }

    // DB로 조회수 관리 로직
//    @Transactional
//    public ShowResponseDto getShow(Long showId, AuthUser authUser) {
//        Show show = findShow(showId);
//        User user = userService.findById(authUser.getId()).orElseThrow(()-> new EntityNotFoundException("회원을 찾지 못했습니다."));
//
//        try {
//            viewCountRepository.save(new ViewCount(user, show));
//        } catch (DataIntegrityViolationException e) {
//            // 이미 존재하면 무시
//        }
//        long viewCount = viewCountRepository.countByShow(show);
//
//        return ShowResponseDto.toDto(show, viewCount);
//    }


    @Transactional(readOnly = true)
    public Show getShowEntity(Long showId) {
        return findShow(showId);
    }

    /**
     * 특정 공연 정보 수정
     *
     * @param showId (수정할 공연 Id)
     * @param requestDto (공연명, 공연 분류, 공연 상세 정보, 지역, 시작 날짜, 종료 날짜, 예약 시작 날짜, 예약 종료 날짜)
     */
    @Transactional
    public void updateShow(Long showId, UpdateShowRequestDto requestDto) {
        Show findShow = findShow(showId);

        findShow.updateShow(requestDto);
    }

    /**
     * 특정 공연 삭제
     *
     * @param showId (삭제할 공연 Id)
     */
    @Transactional
    public void deleteShow(Long showId) {
        Show findShow = findShow(showId);

        findShow.deleteShow();

        // Redis 조회수 관련 데이터 삭제
        redisTemplate.opsForZSet().remove("viewCount", String.valueOf(showId));

        // 유저별 중복 방지 키 삭제 (패턴 기반)
        Set<String> keys = redisTemplate.keys("view:" + showId + ":*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private Show findShow(Long showId) {
        return showRepository.findShowById(showId).orElseThrow(() -> new ShowNotFoundException("해당 공연을 찾을 수 없습니다."));
    }
}
