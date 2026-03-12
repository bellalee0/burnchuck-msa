package com.example.burnchuck.domain.meeting.repository;

import static com.example.burnchuck.common.entity.QMeeting.meeting;
import static com.example.burnchuck.common.entity.QUser.user;
import static com.example.burnchuck.common.entity.QUserMeeting.userMeeting;

import com.example.burnchuck.common.entity.QUserMeeting;
import com.example.burnchuck.common.entity.User;
import com.example.burnchuck.common.entity.UserMeeting;
import com.example.burnchuck.common.enums.MeetingRole;
import com.example.burnchuck.common.enums.MeetingStatus;
import com.example.burnchuck.domain.meeting.dto.response.MeetingSummaryWithStatusResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserMeetingCustomRepositoryImpl implements UserMeetingCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MeetingSummaryWithStatusResponse> findAllMeetingsByUser(User user) {

        QUserMeeting attendee = new QUserMeeting("attendee");

        return queryFactory
            .select(Projections.constructor(
                MeetingSummaryWithStatusResponse.class,
                meeting.id,
                meeting.title,
                meeting.imgUrl,
                meeting.location,
                meeting.meetingDateTime,
                meeting.status.stringValue(),
                meeting.maxAttendees,
                attendee.id.count()
            ))
            .from(userMeeting)
            .join(userMeeting.meeting, meeting)
            .join(attendee).on(attendee.meeting.eq(meeting))
            .where(
                userMeeting.user.eq(user)
            )
            .groupBy(meeting.id)
            .fetch();
    }

    /**
     * 참가 신청한 모임 중 COMPLETED 되지 않은 모임 조회(유저 삭제 시 처리용)
     */
    @Override
    public List<UserMeeting> findActiveMeetingsByUser(User user) {

        return queryFactory
            .selectFrom(userMeeting)
            .join(userMeeting.meeting, meeting)
            .where(
                userMeeting.user.eq(user),
                userMeeting.meetingRole.eq(MeetingRole.PARTICIPANT),
                meeting.status.ne(MeetingStatus.COMPLETED)
            )
            .groupBy(meeting.id)
            .fetch();
    }

    @Override
    public List<UserMeeting> findMeetingMembers(Long meetingId) {

        return queryFactory
                .selectFrom(userMeeting)
                .join(userMeeting.user, user).fetchJoin()
                .where(userMeeting.meeting.id.eq(meetingId))
                .fetch();
    }
}
