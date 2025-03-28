package com.example.sparta_ticketing.domain.reserve.service;

import com.example.sparta_ticketing.common.exception.InvalidRequestException;
import com.example.sparta_ticketing.common.redis.LockService;
import com.example.sparta_ticketing.domain.auth.entity.AuthUser;
import com.example.sparta_ticketing.domain.reserve.dto.request.ReserveCancelRequest;
import com.example.sparta_ticketing.domain.reserve.dto.request.ReserveRequest;
import com.example.sparta_ticketing.domain.reserve.dto.response.ReserveResponse;
import com.example.sparta_ticketing.domain.reserve.entity.Reserve;
import com.example.sparta_ticketing.domain.reserve.repository.ReserveRepository;
import com.example.sparta_ticketing.domain.seat.entity.Seat;
import com.example.sparta_ticketing.domain.seat.service.SeatService;
import com.example.sparta_ticketing.domain.show.entity.Show;
import com.example.sparta_ticketing.domain.show.service.ShowService;
import com.example.sparta_ticketing.domain.user.entity.User;
import com.example.sparta_ticketing.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReserveService {
    private final RedisTemplate<String, String> redisTemplate;
    private final UserService userService;
    private final ShowService showService;
    private final SeatService seatService;
    private final ReserveRepository reserveRepository;
    private final LockService lockService;


    @Transactional
    public ReserveResponse reserve(AuthUser authUser, ReserveRequest reserveRequest) {
        String lockKey = "lock:seat:" + reserveRequest.getShowId() + ":" + reserveRequest.getSeatName();

        return lockService.executeLock(lockKey, ()->{
            String seatKey = "reserve:seat:" + reserveRequest.getShowId() + ":" + reserveRequest.getSeatName();
            String value = redisTemplate.opsForValue().get(seatKey);

            if (value == null) throw new InvalidRequestException("선택하신 좌석은 없는 좌석입니다.");

            int current = Integer.parseInt(value);

            if(current < 1) throw new InvalidRequestException("매진되었습니다.");

            redisTemplate.opsForValue().decrement(seatKey, 1);

            try{
                User user = userService.findById(authUser.getId())
                        .orElseThrow(() -> new InvalidRequestException("User not found"));
                Show show = showService.getShowEntity(reserveRequest.getShowId());
                Seat seat = seatService.getSeatByShowIdAndSeatName(show.getId(), reserveRequest.getSeatName());

                Reserve reserve = new Reserve(user, show, seat.getName(), seat.getPrice());
                Reserve savedReserve = reserveRepository.save(reserve);
                return ReserveResponse.toDto(savedReserve);
            } catch (Exception e) {
                redisTemplate.opsForValue().increment(seatKey);
                throw new InvalidRequestException("데이터베이스에 저장 중 오류 발생");
            }
        });
    }

    @Transactional
    public void reserveCancel(AuthUser authUser, ReserveCancelRequest request) {
        Reserve reserve = reserveRepository.findById(request.getReserveId())
                .orElseThrow(()-> new InvalidRequestException("예약정보를 찾을 수 없습니다."));
        if(reserve.getUser().getId() != authUser.getId()) {
            throw new InvalidRequestException("본인 예약만 취소할 수 있습니다.");
        }

        String seatKey = "reserve:seat:" + reserve.getShow().getId() + ":" + reserve.getSeatName();
        redisTemplate.opsForValue().increment(seatKey, 1);

        try {
            reserveRepository.delete(reserve);
        } catch (Exception e) {
            redisTemplate.opsForValue().decrement(seatKey, 1);
            throw new RuntimeException("예약 취소 처리 중 오류가 발생했습니다.");
        }

    }
}
