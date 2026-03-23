package com.talentFlow.progress.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.enums.EnrollmentStatus;

import java.math.BigDecimal;

public interface ProgressUpdatePublisher {
    void publish(User learner, Course course, BigDecimal progressPct, EnrollmentStatus status);
}
