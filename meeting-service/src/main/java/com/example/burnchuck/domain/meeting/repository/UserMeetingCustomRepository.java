package com.example.burnchuck.domain.meeting.repository;

import com.example.burnchuck.common.entity.User;
import com.example.burnchuck.common.entity.UserMeeting;
import com.example.burnchuck.domain.meeting.dto.response.MeetingSummaryWithStatusResponse;
import java.util.List;

public interface UserMeetingCustomRepository {

    List<MeetingSummaryWithStatusResponse> findAllMeetingsByUser(User user);

    List<UserMeeting> findActiveMeetingsByUser(User user);

    List<UserMeeting> findMeetingMembers(Long meetingId);
}
