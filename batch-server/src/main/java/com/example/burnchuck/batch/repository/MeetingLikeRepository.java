package com.example.burnchuck.batch.repository;

import com.example.burnchuck.common.entity.MeetingLike;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingLikeRepository extends JpaRepository<MeetingLike, Long> {

    @Query("""
        SELECT ml.meeting.id AS meetingId,
               COUNT(ml) AS likeCount
        FROM MeetingLike ml
        WHERE ml.meeting.id IN :meetingIds
        GROUP BY ml.meeting.id
    """)
    List<Object[]> findLikeCountsGroupedByMeetingId(@Param("meetingIds") Set<Long> meetingIds);
}
