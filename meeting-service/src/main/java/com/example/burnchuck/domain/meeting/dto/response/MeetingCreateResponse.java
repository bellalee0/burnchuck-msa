package com.example.burnchuck.domain.meeting.dto.response;

import com.example.burnchuck.common.entity.Meeting;
import com.example.burnchuck.common.enums.MeetingStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MeetingCreateResponse {

    private final Long meetingId;
    private final String title;
    private final MeetingStatus status;

    public static MeetingCreateResponse from(Meeting meeting) {
        return new MeetingCreateResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getStatus()
        );
    }
}
