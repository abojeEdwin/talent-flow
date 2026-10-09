package com.talentFlow.repository;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends MongoRepository<Notification, UUID> {
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Optional<Notification> findByIdAndUser(UUID id, User user);

    List<Notification> findByUserAndReadFalse(User user);
}