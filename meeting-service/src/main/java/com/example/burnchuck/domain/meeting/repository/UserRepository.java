package com.example.burnchuck.domain.meeting.repository;

import com.example.burnchuck.common.entity.User;
import com.example.burnchuck.common.enums.ErrorCode;
import com.example.burnchuck.common.exception.CustomException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndDeletedFalse(Long id);

    @Query("""
        SELECT u
        FROM User u JOIN FETCH u.address
        WHERE u.id = :id AND u.deleted = false
        """)
    Optional<User> findActiveUserByIdWithAddress(@Param("id") Long id);

    default User findActivateUserById(Long id) {
        return findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    default User findActivateUserWithAddress(Long id) {
        return findActiveUserByIdWithAddress(id)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
