package com.example.burnchuck.domain.meetingLike.service;

import static com.example.burnchuck.common.enums.ErrorCode.ALREADY_LIKED_MEETING;

import com.example.burnchuck.common.dto.AuthUser;
import com.example.burnchuck.common.entity.Meeting;
import com.example.burnchuck.common.entity.MeetingLike;
import com.example.burnchuck.common.entity.User;
import com.example.burnchuck.common.exception.CustomException;
import com.example.burnchuck.domain.meeting.repository.MeetingRepository;
import com.example.burnchuck.domain.meeting.repository.UserRepository;
import com.example.burnchuck.domain.meetingLike.dto.response.MeetingLikeCountResponse;
import com.example.burnchuck.domain.meetingLike.dto.response.MeetingLikeResponse;
import com.example.burnchuck.domain.meetingLike.event.MeetingLikeEventPublisher;
import com.example.burnchuck.domain.meetingLike.repository.MeetingLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingLikeService {

    private final MeetingLikeRepository meetingLikeRepository;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingLikeEventPublisher eventPublisher;

    /**
     *  좋아요 생성
     */
    @Transactional
    public MeetingLikeResponse createLike(AuthUser authUser, Long meetingId) {

        User user = userRepository.findActivateUserById(authUser.getId());

        Meeting meeting = meetingRepository.findActivateMeetingById(meetingId);

        if (meetingLikeRepository.existsByUserAndMeeting(user, meeting)) {
            throw new CustomException(ALREADY_LIKED_MEETING);
        }

        MeetingLike meetingLike = new MeetingLike(user, meeting);
        meetingLikeRepository.save(meetingLike);

        eventPublisher.likeIncreaseEvent(meeting.getId());

        return MeetingLikeResponse.from(meeting);
    }

    /**
     *  좋아요 취소
     */
    @Transactional
    public void deleteLike(AuthUser authUser, Long meetingId) {

        User user = userRepository.findActivateUserById(authUser.getId());

        Meeting meeting = meetingRepository.findActivateMeetingById(meetingId);

        MeetingLike meetingLike = meetingLikeRepository.findByUserAndMeetingOrThrow(user, meeting);

        meetingLikeRepository.delete(meetingLike);

        eventPublisher.likeDecreaseEvent(meeting.getId());
    }

    /**
     *  모임 별 좋아요 개수 조회
     */
    @Transactional(readOnly = true)
    public MeetingLikeCountResponse countLikes(Long meetingId) {

        Meeting meeting = meetingRepository.findActivateMeetingById(meetingId);

        long likes = meetingLikeRepository.countByMeeting(meeting);

        return MeetingLikeCountResponse.of(likes);
    }

    /**
     *  모임 좋아요 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean checkLikeExistence(Long meetingId, AuthUser authUser) {

        if (authUser == null) {
            return false;
        }

        User user = userRepository.findActivateUserById(authUser.getId());

        Meeting meeting = meetingRepository.findActivateMeetingById(meetingId);

        return meetingLikeRepository.existsByUserAndMeeting(user, meeting);
    }
}
