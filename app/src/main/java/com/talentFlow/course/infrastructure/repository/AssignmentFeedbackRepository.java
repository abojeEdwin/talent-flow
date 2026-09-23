package com.talentFlow.course.infrastructure.repository;

import com.talentFlow.course.domain.AssignmentFeedback;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface AssignmentFeedbackRepository extends MongoRepository<AssignmentFeedback, UUID> {
}