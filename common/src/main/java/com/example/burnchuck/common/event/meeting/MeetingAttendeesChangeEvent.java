package com.example.burnchuck.common.event.meeting;

import com.example.burnchuck.common.entity.Meeting;
import com.example.burnchuck.common.entity.User;
import com.example.burnchuck.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingAttendeesChangeEvent {

    private NotificationType type;
    private Meeting meeting;
    private User user;
}
