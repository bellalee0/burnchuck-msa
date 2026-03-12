package com.example.burnchuck.domain.user.event;

import com.example.burnchuck.common.event.user.UserDeleteEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishUserDeletedEvent(Long userId) {

        UserDeleteEvent event = new UserDeleteEvent(userId);
        publisher.publishEvent(event);
    }
}
