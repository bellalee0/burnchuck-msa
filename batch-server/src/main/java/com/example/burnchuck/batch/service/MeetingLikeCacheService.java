package com.example.burnchuck.batch.service;

import static com.example.burnchuck.common.constants.CacheKey.LIKE_COUNT_KEY;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingLikeCacheService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 날짜 포함 키 생성
     */
    public String generateKey() {
        return String.format(LIKE_COUNT_KEY, LocalDate.now());
    }

    /**
     * 당일 생성된 내용 모두 조회
     */
    public Set<TypedTuple<String>> getLikeKeyList() {

        String key = generateKey();

        if (!redisTemplate.hasKey(key)) {
            return Collections.emptySet();
        }

        return redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
    }

    /**
     * Zset 멤버 삭제
     */
    public void clearLikeKey(Set<Long> meetingIdList) {

        for (Long meetingId : meetingIdList) {
            redisTemplate.opsForZSet().remove(generateKey(), String.valueOf(meetingId));
        }
    }
}
