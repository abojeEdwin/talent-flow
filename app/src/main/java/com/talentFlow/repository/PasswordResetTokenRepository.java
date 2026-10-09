package com.talentFlow.repository;

import com.talentFlow.auth.data.entity.PasswordResetToken;
import com.talentFlow.auth.data.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}