package com.example.burnchuck.domain.meetingLike.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingLikeCacheService {

    private final StringRedisTemplate redisTemplate;

    private static final String LIKE_COUNT_KEY = "like::meeting::%s";
    private static final int LIKE_COUNT_TTL = 3; // 3일

    /**
     * 좋아요 생성 시 Zset 값 증가
     */
    public void increaseMeetingLike(Long meetingId) {

        String key = generateKey();
        redisTemplate.opsForZSet().incrementScore(key, String.valueOf(meetingId), 1);

        redisTemplate.expire(key, LIKE_COUNT_TTL, TimeUnit.DAYS);
    }

    /**
     * 좋아요 삭제 시 Zset 값 감소
     */
    public void decreaseMeetingLike(Long meetingId) {

        String key = generateKey();
        redisTemplate.opsForZSet().incrementScore(key, String.valueOf(meetingId), -1);

        redisTemplate.expire(key, LIKE_COUNT_TTL, TimeUnit.DAYS);
    }

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
