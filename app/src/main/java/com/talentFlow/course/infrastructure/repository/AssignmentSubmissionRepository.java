package com.talentFlow.course.infrastructure.repository;

import com.talentFlow.auth.domain.User;
import com.talentFlow.course.domain.Assignment;
import com.talentFlow.course.domain.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, UUID> {
    List<AssignmentSubmission> findByAssignmentInAndLearnerUser(List<Assignment> assignments, User learnerUser);
}
