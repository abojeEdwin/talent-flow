package com.talentFlow.service;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.entity.Course;

public interface CertificateService {
    void queueCourseCertificate(User learner, Course course);
}
