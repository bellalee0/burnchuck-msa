package com.example.burnchuck.domain.meeting.service;

import com.example.burnchuck.common.dto.AuthUser;
import com.example.burnchuck.common.enums.ErrorCode;
import com.example.burnchuck.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedissonLockAttendanceService {

    private final AttendanceService attendanceService;
    private final RedissonClient redissonClient;

    public static final String LOCK_KEY_PREFIX = "meeting_lock:";
    public static final Long WAIT_TIME = 5L;
    public static final Long LEASE_TIME = 10L;

    public void registerAttendance(AuthUser authUser, Long meetingId) {

        String lockKey = LOCK_KEY_PREFIX + meetingId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            attendanceService.registerAttendance(authUser, meetingId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
