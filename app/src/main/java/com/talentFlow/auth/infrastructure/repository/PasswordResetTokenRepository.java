package com.talentFlow.auth.infrastructure.repository;

import com.talentFlow.auth.domain.PasswordResetToken;
import com.talentFlow.auth.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}