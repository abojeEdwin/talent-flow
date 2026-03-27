package com.talentFlow.auth.infrastructure.repository;

import com.talentFlow.auth.domain.PasswordResetToken;
import com.talentFlow.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}
