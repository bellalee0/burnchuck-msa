package com.example.burnchuck.common.event.meetingLike;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingLikeEvent {

    private MeetingLikeEventType type;
    private Long meetingId;

    public enum MeetingLikeEventType {
        INCREASE, DECREASE
    }
}
