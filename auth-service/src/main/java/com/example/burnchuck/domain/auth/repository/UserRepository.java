package com.example.burnchuck.domain.auth.repository;

import com.example.burnchuck.common.entity.User;
import com.example.burnchuck.common.enums.ErrorCode;
import com.example.burnchuck.common.enums.Provider;
import com.example.burnchuck.common.exception.CustomException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    default User findActivateUserByEmail(String email) {
        return findByEmailAndDeletedFalse(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}

