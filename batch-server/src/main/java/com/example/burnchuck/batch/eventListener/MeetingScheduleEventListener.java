package com.example.burnchuck.batch.eventListener;

import com.example.burnchuck.batch.repository.MeetingRepository;
import com.example.burnchuck.batch.service.MeetingSchedulingService;
import com.example.burnchuck.common.entity.Meeting;
import com.example.burnchuck.common.enums.MeetingTaskType;
import com.example.burnchuck.common.event.meeting.MeetingEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "ScheduleEventHandler")
public class MeetingScheduleEventListener {

    private final MeetingSchedulingService schedulingService;
    private final MeetingRepository meetingRepository;

    /**
     * 어플리케이션 재시작 후, 삭제된 이벤트 복구
     */
    @EventListener(ApplicationReadyEvent.class)
    public void restoreSchedules() {

        List<Meeting> meetingList = meetingRepository.findActivateMeetingsForSchedules();

        meetingList.forEach(meeting -> {
            schedulingService.scheduleMeetingStatusComplete(meeting);
            schedulingService.scheduleNotification(meeting);
        });

        LocalDate today = LocalDate.now();
        LocalDateTime startDate = today.minusDays(1).atStartOfDay();
        LocalDateTime endDate = today.plusDays(1).atStartOfDay();

        List<Meeting> requireNotificationList = meetingRepository.findActivateMeetingsForNotification(startDate, endDate);

        requireNotificationList.forEach(schedulingService::scheduleNotification);
    }

    /**
     * MeetingCreatedEvent에 대한 Handler -> TaskSchedule 생성
     */
    @Async("customTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @EventListener
    public void meetingScheduleEventHandler(MeetingEvent event) {

        MeetingTaskType type = event.getType();
        Meeting meeting = event.getMeeting();

        try {
            switch (type) {
                case CREATE -> {
                    schedulingService.scheduleMeetingStatusComplete(meeting);
                    schedulingService.scheduleNotification(meeting);
                }
                case UPDATE -> {
                    schedulingService.scheduleCancel(meeting.getId());

                    schedulingService.scheduleMeetingStatusComplete(meeting);
                    schedulingService.scheduleNotification(meeting);
                }
                case DELETE -> schedulingService.scheduleCancel(meeting.getId());
            }
        } catch (Exception e) {
            log.error("스케줄러 생성 실패 : {}", meeting.getId());
        }
    }
}
