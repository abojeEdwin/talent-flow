package com.talentFlow.progress.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.course.domain.Course;
import com.talentFlow.progress.web.dto.ProgressComputationResult;

public interface ProgressTrackingService {
    ProgressComputationResult recalculateEnrollmentProgress(User learner, Course course);
}
