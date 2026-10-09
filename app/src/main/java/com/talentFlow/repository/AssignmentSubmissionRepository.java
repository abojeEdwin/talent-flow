package com.talentFlow.repository;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.entity.Assignment;
import com.talentFlow.data.entity.AssignmentSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface AssignmentSubmissionRepository extends MongoRepository<AssignmentSubmission, UUID> {
    List<AssignmentSubmission> findByAssignmentInAndLearnerUser(List<Assignment> assignments, User learnerUser);

    boolean existsByAssignment(Assignment assignment);
}