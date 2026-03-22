package com.talentFlow.admin.infrastructure.repository;

import com.talentFlow.admin.domain.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, UUID> {
}
