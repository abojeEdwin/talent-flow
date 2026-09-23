package com.talentFlow.admin.infrastructure.repository;

import com.talentFlow.admin.domain.AdminAuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface AdminAuditLogRepository extends MongoRepository<AdminAuditLog, UUID> {
}