package com.example.burnchuck.common.event.meeting;

import com.example.burnchuck.common.entity.Meeting;
import com.example.burnchuck.common.entity.User;
import com.example.burnchuck.common.enums.MeetingStatus;
import com.example.burnchuck.common.enums.MeetingTaskType;
import com.example.burnchuck.common.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishMeetingCreatedEvent(Meeting meeting, User hostUser) {

        MeetingEvent event = new MeetingEvent(MeetingTaskType.CREATE, meeting, hostUser);
        publisher.publishEvent(event);
    }

    public void publishMeetingUpdatedEvent(Meeting meeting) {

        MeetingEvent event = new MeetingEvent(MeetingTaskType.UPDATE, meeting);
        publisher.publishEvent(event);
    }

    public void publishMeetingDeletedEvent(Meeting meeting) {

        MeetingEvent event = new MeetingEvent(MeetingTaskType.DELETE, meeting);
        publisher.publishEvent(event);
    }

    public void publishMeetingStatusChangeEvent(Meeting meeting, MeetingStatus status) {

        MeetingStatusChangeEvent event = new MeetingStatusChangeEvent(meeting, status);
        publisher.publishEvent(event);
    }

    public void publishMeetingAttendeesChangeEvent(NotificationType type, Meeting meeting, User user) {

        MeetingAttendeesChangeEvent event = new MeetingAttendeesChangeEvent(type, meeting, user);
        publisher.publishEvent(event);
    }
}
