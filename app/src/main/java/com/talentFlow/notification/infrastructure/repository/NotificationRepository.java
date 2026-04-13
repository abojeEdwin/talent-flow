package com.talentFlow.notification.infrastructure.repository;

import com.talentFlow.auth.domain.User;
import com.talentFlow.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Optional<Notification> findByIdAndUser(UUID id, User user);
}
