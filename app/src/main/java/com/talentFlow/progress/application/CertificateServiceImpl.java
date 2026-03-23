package com.talentFlow.progress.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.course.domain.Course;
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
