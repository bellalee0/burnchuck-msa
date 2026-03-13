package com.example.burnchuck.domain.meetingLike.service;

import com.example.burnchuck.common.event.meetingLike.MeetingLikeEvent;
import com.example.burnchuck.common.event.user.UserDeleteEvent;
import com.example.burnchuck.domain.meetingLike.repository.MeetingLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MeetingLikeEventListener {

    private final MeetingLikeCacheService meetingLikeCacheService;
    private final MeetingLikeRepository meetingLikeRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void meetingLikeCountChange(MeetingLikeEvent event) {

        Long meetingId = event.getMeetingId();

        switch (event.getType()) {
            case INCREASE -> meetingLikeCacheService.increaseMeetingLike(meetingId);
            case DECREASE -> meetingLikeCacheService.decreaseMeetingLike(meetingId);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    @Transactional
    public void deletedUser(UserDeleteEvent event) {

        meetingLikeRepository.deleteByUserId(event.getUserId());
    }
}
