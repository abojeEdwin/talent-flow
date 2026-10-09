package com.talentFlow.repository;

import com.talentFlow.data.entity.AdminAuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface AdminAuditLogRepository extends MongoRepository<AdminAuditLog, UUID> {
}