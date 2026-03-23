package com.talentFlow.progress.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.course.domain.Course;

public interface CertificateService {
    void queueCourseCertificate(User learner, Course course);
}
