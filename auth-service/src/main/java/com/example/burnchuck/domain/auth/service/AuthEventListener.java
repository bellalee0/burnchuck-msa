package com.example.burnchuck.domain.auth.service;

import com.example.burnchuck.common.event.user.UserDeleteEvent;
import com.example.burnchuck.domain.auth.repository.UserRefreshRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final UserRefreshRepository userRefreshRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    @Transactional
    public void deletedUser(UserDeleteEvent event) {

        userRefreshRepository.deleteByUserId(event.getUserId());
    }
}
