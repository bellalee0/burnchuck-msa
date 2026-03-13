package com.example.burnchuck.domain.meetingLike.service;

import static com.example.burnchuck.common.constants.CacheKey.LIKE_COUNT_KEY;
import static com.example.burnchuck.common.constants.CacheKey.LIKE_COUNT_TTL;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingLikeCacheService {

    private final StringRedisTemplate redisTemplate;

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
}
