package com.example.burnchuck.domain.meeting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendeeResponse {

    private final Long attendeeId;
    private final String attendeeProfileImgUrl;
    private final String attendeeNickname;
}
