package com.example.burnchuck.common.event.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDeleteEvent {

    private Long userId;
}
