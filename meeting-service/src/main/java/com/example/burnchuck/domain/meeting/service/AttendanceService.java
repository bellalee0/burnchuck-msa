package com.example.burnchuck.domain.meeting.service;

import com.example.burnchuck.common.dto.AuthUser;
import com.example.burnchuck.common.entity.Meeting;
import com.example.burnchuck.common.entity.User;
import com.example.burnchuck.common.entity.UserMeeting;
import com.example.burnchuck.common.enums.ErrorCode;
import com.example.burnchuck.common.enums.MeetingRole;
import com.example.burnchuck.common.enums.MeetingStatus;
import com.example.burnchuck.common.enums.NotificationType;
import com.example.burnchuck.common.exception.CustomException;
import com.example.burnchuck.domain.meeting.dto.response.AttendanceGetMeetingListResponse;
import com.example.burnchuck.domain.meeting.dto.response.MeetingSummaryWithStatusResponse;
import com.example.burnchuck.domain.meeting.event.MeetingEventPublisher;
import com.example.burnchuck.domain.meeting.repository.MeetingRepository;
import com.example.burnchuck.domain.meeting.repository.UserMeetingRepository;
import com.example.burnchuck.domain.meeting.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final UserMeetingRepository userMeetingRepository;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;

    private final MeetingEventPublisher meetingEventPublisher;

    /**
     * 모임 참여 신청
     * TODO: chatRoomService.joinGroupChatRoom 이벤트 기반으로 변경
     */
    @Transactional
    public void registerAttendance(AuthUser authUser, Long meetingId) {

        User user = userRepository.findActivateUserById(authUser.getId());
        Meeting meeting = meetingRepository.findActivateMeetingById(meetingId);

        if (!meeting.isOpen()) {
            throw new CustomException(ErrorCode.ATTENDANCE_CANNOT_REGISTER);
        }

        if (userMeetingRepository.existsByUserIdAndMeetingId(user.getId(), meeting.getId())) {
            throw new CustomException(ErrorCode.ATTENDANCE_ALREADY_REGISTERED);
        }

        int maxAttendees = meeting.getMaxAttendees();
        int currentAttendees = userMeetingRepository.countByMeeting(meeting);

        if (currentAttendees >= maxAttendees) {
            throw new CustomException(ErrorCode.ATTENDANCE_MAX_CAPACITY_REACHED);
        }

        UserMeeting userMeeting = new UserMeeting(user, meeting, MeetingRole.PARTICIPANT);

        userMeetingRepository.save(userMeeting);

        if (currentAttendees +1 == maxAttendees) {
            meeting.updateStatus(MeetingStatus.CLOSED);
            meetingEventPublisher.publishMeetingStatusChangeEvent(meeting, MeetingStatus.CLOSED);
        }

        meetingEventPublisher.publishMeetingAttendeesChangeEvent(NotificationType.MEETING_MEMBER_JOIN, meeting, user);
    }

    /**
     * 모임 참여 취소
     * TODO: chatRoomService.leaveChatRoomRegardlessOfStatus 이벤트 기반으로 변경
     */
    @Transactional
    public void cancelAttendance(AuthUser authUser, Long meetingId) {

        User user = userRepository.findActivateUserById(authUser.getId());

        Meeting meeting = meetingRepository.findActivateMeetingById(meetingId);

        if (meeting.isCompleted()) {
            throw new CustomException(ErrorCode.ATTENDANCE_CANNOT_CANCEL_WHEN_MEETING_CLOSED);
        }

        UserMeeting userMeeting = userMeetingRepository.findUserMeeting(user.getId(), meeting.getId());

        if (userMeeting.isHost()) {
            throw new CustomException(ErrorCode.ATTENDANCE_HOST_CANNOT_CANCEL);
        }

        userMeetingRepository.delete(userMeeting);

        if (meeting.isClosed()) {
            meeting.updateStatus(MeetingStatus.OPEN);
            meetingEventPublisher.publishMeetingStatusChangeEvent(meeting, MeetingStatus.OPEN);
        }

        meetingEventPublisher.publishMeetingAttendeesChangeEvent(NotificationType.MEETING_MEMBER_LEFT, meeting, user);
    }

    /**
     * 유저 삭제 후, 참가 신청한 모임 취소 처리
     * TODO: chatRoomService.leaveChatRoomRegardlessOfStatus 이벤트 기반으로 변경
     */
    @Transactional
    public void cancelAllAttendanceAfterDeleteUser(Long userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<UserMeeting> userMeetingList = userMeetingRepository.findActiveMeetingsByUser(user);

        for (UserMeeting userMeeting : userMeetingList) {

            userMeetingRepository.delete(userMeeting);

            Meeting meeting = userMeeting.getMeeting();

            if (meeting.isClosed()) {
                meeting.updateStatus(MeetingStatus.OPEN);
                meetingEventPublisher.publishMeetingStatusChangeEvent(meeting, MeetingStatus.OPEN);
            }

            meetingEventPublisher.publishMeetingAttendeesChangeEvent(NotificationType.MEETING_MEMBER_LEFT, meeting, user);
        }
    }

    /**
     * 참여한 모임 목록 조회
     */
    @Transactional(readOnly = true)
    public AttendanceGetMeetingListResponse getAttendingMeetingList(AuthUser authUser) {

        if (authUser == null) {
            return new AttendanceGetMeetingListResponse(List.of());
        }

        User user = userRepository.findActivateUserById(authUser.getId());

        List<MeetingSummaryWithStatusResponse> meetingList = userMeetingRepository.findAllMeetingsByUser(user);

        return new AttendanceGetMeetingListResponse(meetingList);
    }
}
