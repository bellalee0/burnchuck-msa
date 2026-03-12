package com.example.burnchuck.domain.meeting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MeetingMemberResponse {

    private Long hostId;
    private String hostProfileImgUrl;
    private String hostNickname;

    private List<AttendeeResponse> attendeeList;
}
