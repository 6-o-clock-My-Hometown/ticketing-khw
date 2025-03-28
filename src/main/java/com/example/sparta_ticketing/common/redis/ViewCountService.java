package com.example.sparta_ticketing.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ViewCountService {
    private final RedisTemplate<String, String> redisTemplate; // Redis에 접근하기 위한 템플릿


    // 유저가 해당 공연을 조회한 적이 있는지 확인
    public boolean hasViewed(Long showId, Long userId) {
        String key = "view:" + showId + ":" + userId; // key 형식: view:공연ID:유저ID
        return  Boolean.TRUE.equals(redisTemplate.hasKey(key)); // Redis에 해당 key가 있는지 확인
    }

    // 조회수 증가 로직
    public void increaseViewCount(Long showId, Long userId) {
        if (!hasViewed(showId, userId)) { // 조회 이력이 없다면
            String key = setKey();
            redisTemplate.opsForZSet()
                    .incrementScore(key, String.valueOf(showId), 1); // ZSet에서 공연ID의 점수(조회수) 1 증가

            String viewedKey = "view:" + showId + ":" + userId; // 중복 방지를 위한 키
            redisTemplate.opsForValue().set(viewedKey, "1", Duration.ofDays(1)); // 1일 동안 키 유지 (중복 조회 방지) "1"은 단지 봤다는 표시일 뿐 의미는 없음
        }
    }

    // 해당 공연의 조회수를 가져오는 메서드
    public long getViewCount(Long showId) {
        String key = setKey();

        Double score = redisTemplate.opsForZSet().score(key, String.valueOf(showId)); // ZSet에서 점수(조회수) 조회
        return score == null ? 0 : score.longValue(); // 조회수가 없으면 0 반환
    }

    // 인기 공연 상위 N개를 조회 (조회수 기준 내림차순 정렬)
    public Set<ZSetOperations.TypedTuple<String>> getTopViewed(int count) {
        String key = setKey();

        return redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, count - 1); // 높은 점수부터 정렬된 데이터 가져오기
    }

    /**
     * 매일 자정에 실행 (00:00:00)
     * 어제 날짜의 viewCount ZSet 삭제
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteYesterdayViewCount() {
        String yesterdayKey = "viewCount:" + LocalDate.now().minusDays(1);
        redisTemplate.delete(yesterdayKey);
    }

    private String setKey(){
        return "viewCount:" + LocalDate.now();
    }
}