package com.talentFlow.progress.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseEnrollment;
import com.talentFlow.course.domain.enums.EnrollmentStatus;
import com.talentFlow.course.infrastructure.repository.CourseEnrollmentRepository;
import com.talentFlow.course.infrastructure.repository.LessonProgressRepository;
import com.talentFlow.course.infrastructure.repository.LessonRepository;
import com.talentFlow.progress.web.dto.ProgressComputationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProgressTrackingServiceImpl implements ProgressTrackingService {

    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CertificateService certificateService;
    private final ProgressUpdatePublisher progressUpdatePublisher;

    @Override
    @Transactional
    public ProgressComputationResult recalculateEnrollmentProgress(User learner, Course course) {
        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseAndUser(course, learner)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Learner is not enrolled in the course"));
        if (enrollment.getStatus() == EnrollmentStatus.REVOKED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Enrollment is revoked for this course");
        }

        long totalLessons = lessonRepository.countByModule_Course(course);
        long completedLessons = lessonProgressRepository.countByUserAndCompletedTrueAndLesson_Module_Course(learner, course);

        BigDecimal progressPct = BigDecimal.ZERO;
        if (totalLessons > 0) {
            progressPct = BigDecimal.valueOf(completedLessons)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalLessons), 2, RoundingMode.HALF_UP);
        }

        enrollment.setProgressPct(progressPct);
        boolean certificateQueued = false;
        if (progressPct.compareTo(BigDecimal.valueOf(100)) >= 0 && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
            certificateService.queueCourseCertificate(learner, course);
            certificateQueued = true;
        }
        if (progressPct.compareTo(BigDecimal.valueOf(100)) < 0 && enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            enrollment.setStatus(EnrollmentStatus.ENROLLED);
            enrollment.setCompletedAt(null);
        }

        courseEnrollmentRepository.save(enrollment);
        progressUpdatePublisher.publish(learner, course, enrollment.getProgressPct(), enrollment.getStatus());

        return new ProgressComputationResult(
                enrollment.getProgressPct(),
                enrollment.getStatus(),
                certificateQueued
        );
    }
}
