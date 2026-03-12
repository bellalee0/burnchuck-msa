package com.example.burnchuck.domain.meeting.service;

import com.example.burnchuck.common.event.user.UserDeleteEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MeetingEventListener {

    private final AttendanceService attendanceService;
    private final MeetingService meetingService;

    @Async("customTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void deleteHostedMeetings(UserDeleteEvent event) {

        Long userId = event.getUserId();

        meetingService.deleteAllHostedMeetingsAfterUserDelete(userId);
    }

    @Async("customTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void cancelAttendMeetings(UserDeleteEvent event) {

        Long userId = event.getUserId();

        attendanceService.cancelAllAttendanceAfterDeleteUser(userId);
    }
}
