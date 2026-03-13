package com.example.burnchuck.batch.repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SchedulingRepository {

    private static final String SCHEDULED_TASK_PREFIX = "ScheduledTask::";
    public static final String MEETING_CHANGE_STATUS = "Meeting::";
    public static final String NOTIFICATION_REVIEW_REQUEST = "Notification::";

    private final ConcurrentHashMap<String, ScheduledFuture<?>> repository = new ConcurrentHashMap<>();

    /**
     * task 저장
     */
    public void save(Long targetId, String actionName, ScheduledFuture<?> task) {
        repository.put(SCHEDULED_TASK_PREFIX + actionName + targetId, task);
    }

    /**
     * task cancel
     */
    public void cancel(Long targetId) {

        String changeStatusKey = SCHEDULED_TASK_PREFIX + MEETING_CHANGE_STATUS + targetId;

        ScheduledFuture<?> changeStatusTask = repository.get(changeStatusKey);

        if (changeStatusTask != null) {
            changeStatusTask.cancel(true);
            repository.remove(changeStatusKey);
        }

        String notificationKey = SCHEDULED_TASK_PREFIX + NOTIFICATION_REVIEW_REQUEST + targetId;

        ScheduledFuture<?> notificationTask = repository.get(notificationKey);

        if (notificationTask != null) {
            notificationTask.cancel(true);
            repository.remove(notificationKey);
        }
    }
}
