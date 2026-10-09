package com.talentFlow.service;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.entity.Course;
import com.talentFlow.data.dto.ProgressComputationResult;

public interface ProgressTrackingService {
    ProgressComputationResult recalculateEnrollmentProgress(User learner, Course course);
}
