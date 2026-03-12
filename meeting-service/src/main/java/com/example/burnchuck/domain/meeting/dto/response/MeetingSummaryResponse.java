package com.example.burnchuck.domain.meeting.dto.response;

import com.example.burnchuck.common.entity.Meeting;
import com.example.burnchuck.domain.document.MeetingDocument;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MeetingSummaryResponse {

    private final Long meetingId;
    private final String meetingTitle;
    private final String imgUrl;
    private final String location;
    private final Double latitude;
    private final Double longitude;
    private final LocalDateTime meetingDatetime;
    private final int maxAttendees;
    private final int currentAttendees;

    public MeetingSummaryResponse(MeetingDocument meetingDocument) {
        this.meetingId = meetingDocument.getId();
        this.meetingTitle = meetingDocument.getTitle();
        this.imgUrl = meetingDocument.getImgUrl();
        this.location = meetingDocument.getLocation();
        this.latitude = meetingDocument.getGeoPoint().getLat();
        this.longitude = meetingDocument.getGeoPoint().getLon();
        this.meetingDatetime = meetingDocument.getMeetingDatetime();
        this.maxAttendees = meetingDocument.getMaxAttendees();
        this.currentAttendees = meetingDocument.getCurrentAttendees();
    }

    public static MeetingSummaryResponse from(Meeting meeting, int currentAttendees) {
        return new MeetingSummaryResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getImgUrl(),
                meeting.getLocation(),
                meeting.getLatitude(),
                meeting.getLongitude(),
                meeting.getMeetingDateTime(),
                meeting.getMaxAttendees(),
                currentAttendees
        );
    }
}
