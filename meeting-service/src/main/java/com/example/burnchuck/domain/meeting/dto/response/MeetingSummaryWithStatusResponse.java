package com.example.burnchuck.domain.meeting.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class MeetingSummaryWithStatusResponse {

    private final Long meetingId;
    private final String meetingTitle;
    private final String imgUrl;
    private final String location;
    private final LocalDateTime meetingDatetime;
    private final String status;
    private final int maxAttendees;
    private final Long currentAttendees;
}
