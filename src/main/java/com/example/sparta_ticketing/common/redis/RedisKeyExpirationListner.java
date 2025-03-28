package com.example.sparta_ticketing.common.redis;

import com.example.sparta_ticketing.domain.seat.entity.Seat;
import com.example.sparta_ticketing.domain.seat.repository.SeatRepository;
import com.example.sparta_ticketing.domain.show.entity.Show;
import com.example.sparta_ticketing.domain.show.repository.ShowRepository;
import jakarta.websocket.OnMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class RedisKeyExpirationListner extends KeyExpirationEventMessageListener {
    private final RedisTemplate<String, String> redisTemplate;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;

    public RedisKeyExpirationListner(RedisTemplate<String, String> redisTemplate,
                                     ShowRepository showRepository,
                                     SeatRepository seatRepository,
                                     RedisMessageListenerContainer redisMessageListenerContainer) {
        super(redisMessageListenerContainer);
        this.redisTemplate = redisTemplate;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
    }

    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody()); //message는 body와 getChannel이 key만료 이벤트에 대한 정보가 byte배열로 담겨있음. body에는 reserve:start:{showId} 형식으로 되어있다.
        if (expiredKey.startsWith("reserve:start:")) {
            Long showId = Long.parseLong(expiredKey.split(":")[2]);

            Show show = showRepository.findById(showId).orElseThrow();
            List<Seat> seats = seatRepository.findByShowId(show.getId()).orElseThrow();

            for (Seat seat : seats) {
                String seatKey = "reserve:seat:" + showId + ":" + seat.getName();
                redisTemplate.opsForValue().set(seatKey, String.valueOf(seat.getCount()));
                redisTemplate.expire(seatKey, Duration.between(LocalDateTime.now(), show.getReservationEndDate()));
            }
        }
    }
}
