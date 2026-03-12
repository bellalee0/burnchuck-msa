package com.example.burnchuck.domain.meeting.service;

import com.example.burnchuck.common.entity.Meeting;
import com.example.burnchuck.common.enums.MeetingStatus;
import com.example.burnchuck.common.enums.MeetingTaskType;
import com.example.burnchuck.common.event.meeting.MeetingAttendeesChangeEvent;
import com.example.burnchuck.common.event.meeting.MeetingEvent;
import com.example.burnchuck.common.event.meeting.MeetingStatusChangeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ElasticsearchEventListener {

    private final ElasticsearchService elasticsearchService;

    @Async("customTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void meetingSyncElasticsearch(MeetingEvent event) {

        MeetingTaskType type = event.getType();
        Meeting meeting = event.getMeeting();

        switch (type) {
            case CREATE, UPDATE -> elasticsearchService.saveMeeting(meeting);
            case DELETE -> elasticsearchService.deleteMeeting(meeting);
        }
    }

    @Async("customTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void meetingChangeStatus(MeetingStatusChangeEvent event) {

        MeetingStatus status = event.getStatus();
        Meeting meeting = event.getMeeting();

        switch (status) {
            case OPEN, CLOSED -> elasticsearchService.updateMeetingStatus(meeting.getId(), event.getStatus());
            case COMPLETED -> elasticsearchService.deleteMeeting(meeting);
        }
    }

    @Async("customTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void meetingChangeAttendees(MeetingAttendeesChangeEvent event) {

        elasticsearchService.updateMeetingCurrentAttendees(event.getMeeting());
    }
}
