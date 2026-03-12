package com.example.burnchuck.domain.meeting.dto.response;

import com.example.burnchuck.domain.document.MeetingDocument;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MeetingMapPointResponse {

    private final Long meetingId;
    private final String meetingTitle;
    private final Double latitude;
    private final Double longitude;

    public static MeetingMapPointResponse from(MeetingDocument meetingDocument) {
        return new MeetingMapPointResponse(
            meetingDocument.getId(),
            meetingDocument.getTitle(),
            meetingDocument.getGeoPoint().getLat(),
            meetingDocument.getGeoPoint().getLon()
        );
    }
}
