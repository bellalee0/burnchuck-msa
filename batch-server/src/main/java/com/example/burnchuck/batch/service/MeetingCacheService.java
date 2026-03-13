package com.example.burnchuck.batch.service;

import static com.example.burnchuck.common.constants.CacheKey.VIEW_COUNT_KEY;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MeetingRedisCache")
public class MeetingCacheService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 해당 날짜의 조회수 증가분 조회
     */
    public Set<TypedTuple<String>> getAllViewList(LocalDate localDate) {

        String key = VIEW_COUNT_KEY + localDate;

        if (!redisTemplate.hasKey(key)) {
            return Collections.emptySet();
        }

        return redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
    }
}
