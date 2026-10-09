package com.talentFlow.service;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.entity.Course;
import com.talentFlow.data.Enums.EnrollmentStatus;

import java.math.BigDecimal;

public interface ProgressUpdatePublisher {
    void publish(User learner, Course course, BigDecimal progressPct, EnrollmentStatus status);
}
