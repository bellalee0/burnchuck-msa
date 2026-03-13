package com.example.burnchuck.domain.notification.service;

import com.example.burnchuck.common.enums.MeetingTaskType;
import com.example.burnchuck.common.event.meeting.MeetingAttendeesChangeEvent;
import com.example.burnchuck.common.event.meeting.MeetingEvent;
import com.example.burnchuck.common.event.notification.CommentNotificationEvent;
import com.example.burnchuck.common.event.user.UserDeleteEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final EmitterService emitterService;

    @Async("customTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void createNewFollowerPostNotification(MeetingEvent event) {

        MeetingTaskType type = event.getType();

        if (type == MeetingTaskType.CREATE) {
            notificationService.notifyNewFollowerPost(event.getMeeting());
        }
    }

    @Async("customTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void createMeetingMemberNotification(MeetingAttendeesChangeEvent event) {

        notificationService.notifyMeetingMember(event.getType(), event.getMeeting(), event.getUser());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void createCommentNotification(CommentNotificationEvent event) {

        notificationService.notifyCommentRequest(event.getMeeting());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void deletedUser(UserDeleteEvent event) {

        emitterService.disconnectAllEmittersByUserId(event.getUserId());
    }
}
