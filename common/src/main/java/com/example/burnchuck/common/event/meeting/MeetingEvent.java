package com.example.burnchuck.common.event.meeting;

import com.example.burnchuck.common.entity.Meeting;
import com.example.burnchuck.common.enums.MeetingTaskType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingEvent {

    private MeetingTaskType type;
    private Meeting meeting;
}
