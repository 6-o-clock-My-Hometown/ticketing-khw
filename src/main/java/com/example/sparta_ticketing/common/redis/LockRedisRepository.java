package com.example.sparta_ticketing.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class LockRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration LOCK_TTL = Duration.ofSeconds(3);


    public Boolean lock(String key, String value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, LOCK_TTL);  // setIfAbsent = setnx 명령어와 같음
    }

    public Boolean unlock(String key, String value) {
        String storedValue = redisTemplate.opsForValue().get(key);
        if (Objects.equals(storedValue, value)) { //락 소유자 확인
            return redisTemplate.delete(key);
        }
        return false;
    }

}
