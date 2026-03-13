package com.example.burnchuck.common.event.notification;

import com.example.burnchuck.common.entity.Meeting;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishCommentNotificationEvent(Meeting meeting) {

        CommentNotificationEvent event = new CommentNotificationEvent(meeting);
        publisher.publishEvent(event);
    }
}
