package com.example.burnchuck.domain.meeting.dto.response;

import com.example.burnchuck.common.entity.Meeting;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MeetingUpdateResponse {

    private final Long meetingId;
    private final String title;
    private final String imgUrl;
    private final String description;
    private final String location;
    private final Double latitude;
    private final Double longitude;
    private final LocalDateTime meetingDateTime;

    public static MeetingUpdateResponse from(Meeting meeting) {
        return new MeetingUpdateResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getImgUrl(),
                meeting.getDescription(),
                meeting.getLocation(),
                meeting.getLatitude(),
                meeting.getLongitude(),
                meeting.getMeetingDateTime()
        );
    }
}

