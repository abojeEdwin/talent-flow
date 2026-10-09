package com.talentFlow.service.impl;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.entity.Course;
import com.talentFlow.service.CertificateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CertificateServiceImpl implements CertificateService {

    @Async("progressTaskExecutor")
    @Override
    public void queueCourseCertificate(User learner, Course course) {
        log.info("Queued certificate generation for learner {} on course {}", learner.getId(), course.getId());
    }
}
