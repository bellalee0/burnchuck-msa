package com.example.burnchuck.domain.meetingLike.event;

import com.example.burnchuck.common.event.meetingLike.MeetingLikeEvent.MeetingLikeEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingLikeEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void likeIncreaseEvent(Long meetingId) {
        com.example.burnchuck.common.event.meetingLike.MeetingLikeEvent event = new com.example.burnchuck.common.event.meetingLike.MeetingLikeEvent(MeetingLikeEventType.INCREASE, meetingId);
        publisher.publishEvent(event);
    }

    public void likeDecreaseEvent(Long meetingId) {
        com.example.burnchuck.common.event.meetingLike.MeetingLikeEvent event = new com.example.burnchuck.common.event.meetingLike.MeetingLikeEvent(MeetingLikeEventType.DECREASE, meetingId);
        publisher.publishEvent(event);
    }
}
