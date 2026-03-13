package com.example.burnchuck.common.event.notification;

import com.example.burnchuck.common.entity.Meeting;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentNotificationEvent {

    private Meeting meeting;
}
