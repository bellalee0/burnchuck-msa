package com.example.burnchuck.batch.service;

import com.example.burnchuck.batch.repository.MeetingLikeRepository;
import com.example.burnchuck.batch.repository.MeetingRepository;
import com.example.burnchuck.batch.repository.UserMeetingRepository;
import com.example.burnchuck.common.entity.Meeting;
import com.example.burnchuck.document.MeetingDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EsSyncSchedulerService {

    private final MeetingLikeRepository meetingLikeRepository;
    private final MeetingRepository meetingRepository;
    private final UserMeetingRepository userMeetingRepository;
    private final MeetingLikeCacheService meetingLikeCacheService;

    private final ElasticsearchOperations elasticsearchOperations;

    private static final int BATCH_SIZE = 1000;

    /**
     * 10분마다 좋아요수 ES에 적용
     */
    @Scheduled(fixedDelay = 600000)
    public void syncLikes() {

        Set<TypedTuple<String>> likeSet = meetingLikeCacheService.getLikeKeyList();

        if (likeSet.isEmpty()) {
            return;
        }

        Set<Long> meetingIdSet = likeSet.stream()
            .map(TypedTuple::getValue)
            .map(Long::valueOf)
            .collect(Collectors.toSet());

        Map<Long, Long> totalLikeMap = meetingLikeRepository.findLikeCountsGroupedByMeetingId(meetingIdSet)
            .stream()
            .collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1]
            ));

        List<UpdateQuery> updateQueries = new ArrayList<>();

        for (Map.Entry<Long, Long> entry : totalLikeMap.entrySet()) {

            Long meetingId = entry.getKey();
            Long totalLike = entry.getValue();

            Document document = Document.create();
            document.put("likes", totalLike);

            updateQueries.add(
                UpdateQuery.builder(meetingId.toString())
                    .withDocument(document)
                    .build()
            );
        }

        if (!updateQueries.isEmpty()) {
            elasticsearchOperations.bulkUpdate(updateQueries, IndexCoordinates.of("meetings"));
        }

        meetingLikeCacheService.clearLikeKey(meetingIdSet);
    }

    /**
     * 주 1회 전체 재색인 작업
     */
    // TODO : Alias 기반 재색인 구조 찾아보기
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void syncAllMeetings() {

        long lastId = 0L;

        while (true) {

            List<Meeting> meetingList = meetingRepository.findMeetingListForSync(lastId, PageRequest.of(0, BATCH_SIZE));

            if (meetingList.isEmpty()) {
                break;
            }

            Set<Long> meetingIdSet = meetingList.stream().map(Meeting::getId).collect(Collectors.toSet());

            Map<Long, Long> totalLikeMap = meetingLikeRepository.findLikeCountsGroupedByMeetingId(meetingIdSet)
                .stream()
                .collect(Collectors.toMap(
                    row -> (Long) row[0],
                    row -> (Long) row[1]
                ));

            Map<Long, Integer> currentAttendeesMap = userMeetingRepository.countAllByMeetingIdIn(meetingIdSet)
                .stream()
                .collect(Collectors.toMap(
                    row -> (Long) row[0],
                    row -> (Integer) row[1]
                ));

            List<MeetingDocument> documentList = meetingList.stream()
                .map(meeting -> new MeetingDocument(meeting, totalLikeMap.get(meeting.getId()), currentAttendeesMap.get(meeting.getId())))
                .toList();

            elasticsearchOperations.save(documentList);

            lastId = meetingList.get(meetingList.size() -1).getId();
        }
    }
}
