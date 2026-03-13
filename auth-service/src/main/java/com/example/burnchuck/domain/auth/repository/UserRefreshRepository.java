package com.example.burnchuck.domain.auth.repository;

import com.example.burnchuck.common.entity.UserRefresh;
import com.example.burnchuck.common.enums.ErrorCode;
import com.example.burnchuck.common.exception.CustomException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRefreshRepository extends JpaRepository<UserRefresh, Long> {

    Optional<UserRefresh> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    default UserRefresh findUserRefreshByUserId(Long userId) {
        return findByUserId(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_NOT_FOUND));
    }
}
