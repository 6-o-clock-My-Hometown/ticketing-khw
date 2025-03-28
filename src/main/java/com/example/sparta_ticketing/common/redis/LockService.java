package com.example.sparta_ticketing.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class LockService {
    private final LockRedisRepository lockRedisRepository;

    private static final int MAX_RETRY = 10;
    private static final int RETRY_WAIT = 50; //ms

    public <T> T executeLock(String key, Supplier<T> task) {
        String uuid = UUID.randomUUID().toString();
        boolean lockAcquired = false;

        try {
            for(int i=0; i<MAX_RETRY; i++) {
                if(Boolean.TRUE.equals(lockRedisRepository.lock(key, uuid))) {
                    lockAcquired = true;
                    return task.get(); // 여기서 비즈니스 로직 실행!
                     // 문제점 -> 작업이 아직 안끝났는데 락 유지 TTL이 끝나면 락을 뻇길 수 있지만 작업은 아직 진행 중. 동시성 문제가 발생 할 수 있음 이를 redisson에선 watchdog이 TTL을 자동으로 갱신해 줌.
                }
                Thread.sleep(RETRY_WAIT);
            }
            throw new IllegalStateException("현재 요청이 많습니다. 잠시후 다시 시도해주세요.");

        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 기다리는 중 인터럽트 발생!");
        } finally {
            if(lockAcquired) {
                lockRedisRepository.unlock(key, uuid);
            }
        }

    }
}
