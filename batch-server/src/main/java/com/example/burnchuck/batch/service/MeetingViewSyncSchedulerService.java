package com.example.burnchuck.batch.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Slf4j(topic = "MeetingViewSyncScheduler")
public class MeetingViewSyncSchedulerService {

    private final MeetingCacheService meetingCacheService;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void meetingViewSync() {

        LocalDate dayBeforeTody = LocalDate.now().minusDays(1);

        Set<TypedTuple<String>> allViewList = meetingCacheService.getAllViewList(dayBeforeTody);

        Map<Long, Long> viewMap = new HashMap<>();

        for (TypedTuple<String> tuple : allViewList) {
            Long meetingId = Long.parseLong(tuple.getValue());
            Long viewCount = tuple.getScore().longValue();
            viewMap.put(meetingId, viewCount);
        }

        if (viewMap.isEmpty()) {
            return;
        }

        String sql = "UPDATE meetings SET views = views + ? WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, viewMap.entrySet(), viewMap.size(), (ps, entry) -> {
                ps.setLong(1, entry.getValue());
                ps.setLong(2, entry.getKey());
            }
        );
    }
}
