package com.example.burnchuck.domain.meeting.repository;

import com.example.burnchuck.common.entity.Meeting;
import com.example.burnchuck.common.enums.ErrorCode;
import com.example.burnchuck.common.exception.CustomException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long>, MeetingCustomRepository {

    Optional<Meeting> findByIdAndDeletedFalse(Long id);

    default Meeting findActivateMeetingById(Long id) {
        return findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));
    }
}
