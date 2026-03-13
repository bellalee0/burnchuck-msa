package com.example.burnchuck.batch.repository;

import com.example.burnchuck.common.entity.UserMeeting;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserMeetingRepository extends JpaRepository<UserMeeting, Long> {

    @Query("""
        SELECT um.meeting.id as meetingId, count(um) as attendees
        FROM UserMeeting um
        JOIN um.meeting
        WHERE um.meeting.id in :meetingIdList
        GROUP BY um.meeting.id
        """)
    List<Object[]> countAllByMeetingIdIn(@Param("meetingIdList") Set<Long> meetingIdList);
}
