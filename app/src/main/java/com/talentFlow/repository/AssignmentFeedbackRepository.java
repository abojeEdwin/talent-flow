package com.talentFlow.repository;

import com.talentFlow.data.entity.AssignmentFeedback;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface AssignmentFeedbackRepository extends MongoRepository<AssignmentFeedback, UUID> {
}