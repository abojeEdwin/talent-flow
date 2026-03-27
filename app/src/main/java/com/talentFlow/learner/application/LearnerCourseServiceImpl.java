package com.talentFlow.learner.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseEnrollment;
import com.talentFlow.course.domain.CourseModule;
import com.talentFlow.course.domain.Lesson;
import com.talentFlow.course.domain.LessonProgress;
import com.talentFlow.course.domain.enums.CourseStatus;
import com.talentFlow.course.domain.enums.EnrollmentStatus;
import com.talentFlow.course.infrastructure.repository.CourseEnrollmentRepository;
import com.talentFlow.course.infrastructure.repository.CourseInstructorRepository;
import com.talentFlow.course.infrastructure.repository.CourseModuleRepository;
import com.talentFlow.course.infrastructure.repository.CourseRepository;
import com.talentFlow.course.infrastructure.repository.LessonProgressRepository;
import com.talentFlow.course.infrastructure.repository.LessonRepository;
import com.talentFlow.course.web.dto.CourseDetailResponse;
import com.talentFlow.course.web.dto.CourseModuleResponse;
import com.talentFlow.course.web.dto.CourseResponse;
import com.talentFlow.course.web.dto.LessonCompletionResponse;
import com.talentFlow.course.web.dto.LessonResponse;
import com.talentFlow.progress.web.dto.ProgressComputationResult;
import com.talentFlow.progress.application.ProgressTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearnerCourseServiceImpl implements LearnerCourseService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final ProgressTrackingService progressTrackingService;

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> browsePublishedCourses() {
        return courseRepository.findByStatus(CourseStatus.PUBLISHED).stream()
                .map(this::toCourseResponse)
                .toList();
    }

    @Override
    @Transactional
    public CourseResponse enrollInCourse(UUID courseId, User learner) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only published courses are open for enrollment");
        }

        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseAndUser(course, learner).orElse(null);
        if (enrollment == null) {
            enrollment = new CourseEnrollment();
            enrollment.setCourse(course);
            enrollment.setUser(learner);
            enrollment.setEnrolledAt(LocalDateTime.now());
        }
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setProgressPct(BigDecimal.ZERO);
        enrollment.setCompletedAt(null);
        enrollment.setRevokedAt(null);
        courseEnrollmentRepository.save(enrollment);
        return toCourseResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> myEnrollments(User learner) {
        return courseEnrollmentRepository.findByUserAndStatus(learner, EnrollmentStatus.ENROLLED).stream()
                .map(CourseEnrollment::getCourse)
                .distinct()
                .map(this::toCourseResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetail(UUID courseId, User learner) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseAndUser(course, learner)
                .orElse(null);
        if (course.getStatus() != CourseStatus.PUBLISHED && enrollment == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Course is not available");
        }

        List<CourseModule> modules = courseModuleRepository.findByCourseOrderByPositionAsc(course);
        List<Lesson> lessons = modules.isEmpty()
                ? List.of()
                : lessonRepository.findByModuleInOrderByModule_PositionAscPositionAsc(modules);
        Map<UUID, Boolean> completionMap = buildCompletionMap(learner, lessons);
        Map<UUID, List<Lesson>> lessonsByModule = lessons.stream()
                .collect(Collectors.groupingBy(lesson -> lesson.getModule().getId()));
        List<CourseModuleResponse> moduleResponses = modules.stream()
                .map(module -> toModuleResponse(module, lessonsByModule.getOrDefault(module.getId(), List.of()), completionMap))
                .toList();

        BigDecimal progressPct = enrollment != null ? enrollment.getProgressPct() : BigDecimal.ZERO;
        return new CourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCoverImageUrl(),
                course.getIntroVideoUrl(),
                course.getStatus().name(),
                progressPct,
                moduleResponses
        );
    }

    @Override
    @Transactional
    public LessonCompletionResponse completeLesson(UUID lessonId, User learner) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson not found"));
        Course course = lesson.getModule().getCourse();

        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseAndUser(course, learner)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "You are not enrolled in this course"));
        if (enrollment.getStatus() == EnrollmentStatus.REVOKED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Enrollment for this course is revoked");
        }

        LessonProgress lessonProgress = lessonProgressRepository.findByUserAndLesson(learner, lesson).orElse(null);
        if (lessonProgress == null) {
            lessonProgress = new LessonProgress();
            lessonProgress.setUser(learner);
            lessonProgress.setLesson(lesson);
        }
        lessonProgress.setCompleted(true);
        lessonProgress.setCompletedAt(LocalDateTime.now());
        lessonProgressRepository.save(lessonProgress);

        ProgressComputationResult result = progressTrackingService.recalculateEnrollmentProgress(learner, course);
        return new LessonCompletionResponse(
                lesson.getId(),
                course.getId(),
                result.progressPct(),
                result.enrollmentStatus().name(),
                result.certificateQueued()
        );
    }

    private CourseResponse toCourseResponse(Course course) {
        Set<UUID> instructorIds = courseInstructorRepository.findByCourse(course).stream()
                .map(ci -> ci.getInstructorUser().getId())
                .collect(Collectors.toSet());
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCoverImageUrl(),
                course.getIntroVideoUrl(),
                course.getStatus().name(),
                course.getPublishedAt(),
                course.getArchivedAt(),
                course.getCreatedByUser().getId(),
                instructorIds
        );
    }

    private CourseModuleResponse toModuleResponse(CourseModule module,
                                                  List<Lesson> lessons,
                                                  Map<UUID, Boolean> completionMap) {
        List<LessonResponse> lessonResponses = lessons.stream()
                .map(lesson -> new LessonResponse(
                        lesson.getId(),
                        lesson.getTitle(),
                        lesson.getLessonType().name(),
                        lesson.getPosition(),
                        lesson.getContentUrl(),
                        lesson.getContentText(),
                        completionMap.getOrDefault(lesson.getId(), false)
                ))
                .toList();
        return new CourseModuleResponse(module.getId(), module.getTitle(), module.getPosition(), lessonResponses);
    }

    private Map<UUID, Boolean> buildCompletionMap(User learner, List<Lesson> lessons) {
        if (lessons.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Boolean> completion = new HashMap<>();
        lessonProgressRepository.findByUserAndLessonIn(learner, lessons).forEach(progress ->
                completion.put(progress.getLesson().getId(), progress.isCompleted()));
        return completion;
    }
}
