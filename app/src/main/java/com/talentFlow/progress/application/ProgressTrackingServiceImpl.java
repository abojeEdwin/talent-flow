package com.talentFlow.progress.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.notification.application.NotificationService;
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
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProgressTrackingServiceImpl implements ProgressTrackingService {

    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CertificateService certificateService;
    private final ProgressUpdatePublisher progressUpdatePublisher;
    private final NotificationService notificationService;

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
            notifyCourseCompleted(learner, course, progressPct);
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

    private void notifyCourseCompleted(User learner, Course course, BigDecimal progressPct) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("courseId", course.getId());
        payload.put("courseTitle", course.getTitle());
        payload.put("progressPct", progressPct);
        payload.put("enrollmentStatus", EnrollmentStatus.COMPLETED.name());
        payload.put("certificateQueued", true);

        notificationService.notifyUser(
                learner.getId(),
                "COURSE_COMPLETED",
                "Course completed",
                "Congratulations. You have completed " + course.getTitle() + ". Certificate generation has been queued.",
                payload
        );
    }
}
