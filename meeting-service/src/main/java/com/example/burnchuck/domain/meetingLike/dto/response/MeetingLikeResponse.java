package com.example.burnchuck.domain.meetingLike.dto.response;

import com.example.burnchuck.common.entity.Meeting;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingLikeResponse {

    private final Long meetingId;

    public static MeetingLikeResponse from(Meeting meeting) {
        return new MeetingLikeResponse(
                meeting.getId()
        );
    }
}
