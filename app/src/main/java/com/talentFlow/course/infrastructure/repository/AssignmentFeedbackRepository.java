package com.talentFlow.course.infrastructure.repository;

import com.talentFlow.course.domain.AssignmentFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssignmentFeedbackRepository extends JpaRepository<AssignmentFeedback, UUID> {
}
