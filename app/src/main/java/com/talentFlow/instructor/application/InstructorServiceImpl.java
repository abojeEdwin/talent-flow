package com.talentFlow.instructor.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.common.storage.worker.MediaUploadQueueService;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.common.storage.enums.MediaUploadTargetType;
import com.talentFlow.notification.application.NotificationService;
import com.talentFlow.course.domain.Assignment;
import com.talentFlow.course.domain.AssignmentFeedback;
import com.talentFlow.course.domain.AssignmentSubmission;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseEnrollment;
import com.talentFlow.course.domain.CourseInstructor;
import com.talentFlow.course.domain.CourseModule;
import com.talentFlow.course.domain.Lesson;
import com.talentFlow.course.domain.enums.CourseStatus;
import com.talentFlow.course.domain.enums.EnrollmentStatus;
import com.talentFlow.course.domain.enums.LessonType;
import com.talentFlow.course.domain.enums.LessonUploadStatus;
import com.talentFlow.course.domain.enums.SubmissionStatus;
import com.talentFlow.course.infrastructure.repository.AssignmentFeedbackRepository;
import com.talentFlow.course.infrastructure.repository.AssignmentRepository;
import com.talentFlow.course.infrastructure.repository.AssignmentSubmissionRepository;
import com.talentFlow.course.infrastructure.repository.CourseEnrollmentRepository;
import com.talentFlow.course.infrastructure.repository.CourseInstructorRepository;
import com.talentFlow.course.infrastructure.repository.CourseModuleRepository;
import com.talentFlow.course.infrastructure.repository.CourseRepository;
import com.talentFlow.course.infrastructure.repository.LessonRepository;
import com.talentFlow.course.web.dto.AssignmentFeedbackResponse;
import com.talentFlow.course.web.dto.AssignmentResponse;
import com.talentFlow.course.web.dto.CourseModuleResponse;
import com.talentFlow.course.web.dto.CourseResponse;
import com.talentFlow.course.web.dto.CreateAssignmentRequest;
import com.talentFlow.course.web.dto.CreateCourseModuleRequest;
import com.talentFlow.course.web.dto.CreateCourseRequest;
import com.talentFlow.course.web.dto.CreateLessonRequest;
import com.talentFlow.course.web.dto.LearnerProgressResponse;
import com.talentFlow.course.web.dto.LessonResponse;
import com.talentFlow.course.web.dto.ProvideFeedbackRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorServiceImpl implements InstructorService {

    private final CourseRepository courseRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final AssignmentFeedbackRepository assignmentFeedbackRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final MediaUploadQueueService mediaUploadQueueService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request, User actor) {
        return createCourseWithMedia(request.title(), request.description(), null, null, actor);
    }

    @Override
    @Transactional
    public CourseResponse createCourseWithMedia(String title,
                                                String description,
                                                MultipartFile coverImage,
                                                MultipartFile introVideo,
                                                User actor) {
        ensureInstructor(actor);
        if (title == null || title.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Course title is required");
        }

        Course course = new Course();
        course.setTitle(title.trim());
        course.setDescription(description);
        course.setStatus(CourseStatus.DRAFT);
        course.setCreatedByUser(actor);

        Course saved = courseRepository.save(course);

        if (coverImage != null && !coverImage.isEmpty()) {
            mediaUploadQueueService.enqueue(
                    MediaUploadTargetType.COURSE_COVER,
                    saved.getId(),
                    actor.getId(),
                    "courses/cover-images",
                    coverImage
            );
        }
        if (introVideo != null && !introVideo.isEmpty()) {
            mediaUploadQueueService.enqueue(
                    MediaUploadTargetType.COURSE_INTRO_VIDEO,
                    saved.getId(),
                    actor.getId(),
                    "courses/intro-videos",
                    introVideo
            );
        }

        CourseInstructor ci = new CourseInstructor();
        ci.setCourse(saved);
        ci.setInstructorUser(actor);
        ci.setPrimary(true);
        courseInstructorRepository.save(ci);

        return toCourseResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> listMyCourses(User actor) {
        ensureInstructor(actor);
        return courseInstructorRepository.findByInstructorUser(actor).stream()
                .map(CourseInstructor::getCourse)
                .distinct()
                .map(this::toCourseResponse)
                .toList();
    }

    @Override
    @Transactional
    public CourseModuleResponse createCourseModule(UUID courseId, CreateCourseModuleRequest request, User actor) {
        Course course = getCourseAndCheckInstructor(courseId, actor);
        CourseModule module = new CourseModule();
        module.setCourse(course);
        module.setTitle(request.title().trim());
        module.setPosition(request.position());

        CourseModule saved = courseModuleRepository.save(module);

        return toModuleResponse(saved, new ArrayList<>());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseModuleResponse> listCourseModules(UUID courseId, User actor) {
        Course course = getCourseAndCheckInstructor(courseId, actor);
        List<CourseModule> modules = courseModuleRepository.findByCourseOrderByPositionAsc(course);
        List<Lesson> lessons = lessonRepository.findByModuleInOrderByModule_PositionAscPositionAsc(modules);
        
        var lessonsByModule = lessons.stream().collect(Collectors.groupingBy(l -> l.getModule().getId()));

        return modules.stream()
                .map(module -> toModuleResponse(module, lessonsByModule.getOrDefault(module.getId(), new ArrayList<>())))
                .toList();
    }

    @Override
    @Transactional
    public CourseModuleResponse updateCourseModule(UUID moduleId, CreateCourseModuleRequest request, User actor) {
        CourseModule module = getModuleAndCheckInstructor(moduleId, actor);
        module.setTitle(request.title().trim());
        module.setPosition(request.position());
        CourseModule saved = courseModuleRepository.save(module);
        
        List<Lesson> lessons = lessonRepository.findByModuleOrderByPositionAsc(saved);
        return toModuleResponse(saved, lessons);
    }

    @Override
    @Transactional
    public void deleteCourseModule(UUID moduleId, User actor) {
        CourseModule module = getModuleAndCheckInstructor(moduleId, actor);
        if (lessonRepository.existsByModule(module)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot delete module that contains lessons. Delete the lessons first.");
        }
        courseModuleRepository.delete(module);
    }

    @Override
    @Transactional
    public LessonResponse createLesson(UUID moduleId, CreateLessonRequest request, User actor) {
        CourseModule module = getModuleAndCheckInstructor(moduleId, actor);
        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(request.title().trim());
        lesson.setLessonType(request.lessonType());
        lesson.setPosition(request.position());
        lesson.setContentUrl(request.contentUrl());
        lesson.setContentText(request.contentText());
        lesson.setUploadStatus(LessonUploadStatus.COMPLETED);

        Lesson saved = lessonRepository.save(lesson);
        return toLessonResponse(saved);
    }

    @Override
    @Transactional
    public LessonResponse createLessonWithFile(UUID moduleId,
                                                String title,
                                                LessonType lessonType,
                                                Integer position,
                                                MultipartFile file,
                                                User actor) {
        if (title == null || title.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Lesson title is required");
        }
        if (lessonType == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Lesson type is required");
        }
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Lesson file is required");
        }

        CourseModule module = getModuleAndCheckInstructor(moduleId, actor);

        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(title.trim());
        lesson.setLessonType(lessonType);
        lesson.setPosition(position);
        lesson.setUploadStatus(LessonUploadStatus.PENDING);
        Lesson saved = lessonRepository.save(lesson);

        mediaUploadQueueService.enqueue(
                MediaUploadTargetType.LESSON_CONTENT,
                saved.getId(),
                actor.getId(),
                "courses/" + module.getCourse().getId() + "/lessons",
                file
        );

        return toLessonResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResponse getLesson(UUID lessonId, User actor) {
        Lesson lesson = getLessonAndCheckInstructor(lessonId, actor);
        return toLessonResponse(lesson);
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(UUID lessonId, CreateLessonRequest request, User actor) {
        Lesson lesson = getLessonAndCheckInstructor(lessonId, actor);
        lesson.setTitle(request.title().trim());
        lesson.setLessonType(request.lessonType());
        lesson.setPosition(request.position());
        lesson.setContentUrl(request.contentUrl());
        lesson.setContentText(request.contentText());
        
        Lesson saved = lessonRepository.save(lesson);
        return toLessonResponse(saved);
    }

    @Override
    @Transactional
    public LessonResponse updateLessonWithFile(UUID lessonId,
                                                String title,
                                                LessonType lessonType,
                                                Integer position,
                                                MultipartFile file,
                                                User actor) {
        Lesson lesson = getLessonAndCheckInstructor(lessonId, actor);
        lesson.setTitle(title.trim());
        lesson.setLessonType(lessonType);
        lesson.setPosition(position);

        if (file != null && !file.isEmpty()) {
            lesson.setUploadStatus(LessonUploadStatus.PENDING);
            lesson.setContentUrl(null);
            mediaUploadQueueService.enqueue(
                    MediaUploadTargetType.LESSON_CONTENT,
                    lesson.getId(),
                    actor.getId(),
                    "courses/" + lesson.getModule().getCourse().getId() + "/lessons",
                    file
            );
        }

        Lesson saved = lessonRepository.save(lesson);
        return toLessonResponse(saved);
    }

    @Override
    @Transactional
    public void deleteLesson(UUID lessonId, User actor) {
        Lesson lesson = getLessonAndCheckInstructor(lessonId, actor);
        lessonRepository.delete(lesson);
    }

    @Override
    @Transactional
    public AssignmentResponse createAssignment(UUID courseId, CreateAssignmentRequest request, User actor) {
        Course course = getCourseAndCheckInstructor(courseId, actor);
        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setTitle(request.title().trim());
        assignment.setInstructions(request.instructions());
        assignment.setDueAt(request.dueAt());
        assignment.setMaxScore(request.maxScore());
        assignment.setCreatedByUser(actor);
        Assignment saved = assignmentRepository.save(assignment);
        notifyAssignmentCreated(course, saved);
        return new AssignmentResponse(
                saved.getId(),
                course.getId(),
                saved.getTitle(),
                saved.getInstructions(),
                saved.getDueAt(),
                saved.getMaxScore(),
                actor.getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearnerProgressResponse> monitorLearnerProgress(UUID courseId, User actor) {
        Course course = getCourseAndCheckInstructor(courseId, actor);
        List<Assignment> assignments = assignmentRepository.findByCourse(course);
        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByCourseAndStatus(course, EnrollmentStatus.ENROLLED);

        return enrollments.stream().map(enrollment -> {
            User learner = enrollment.getUser();
            List<AssignmentSubmission> submissions = assignments.isEmpty()
                    ? List.of()
                    : assignmentSubmissionRepository.findByAssignmentInAndLearnerUser(assignments, learner);
            int total = assignments.size();
            int submitted = submissions.size();
            BigDecimal avg = submissions.stream()
                    .map(AssignmentSubmission::getScore)
                    .filter(score -> score != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long scoredCount = submissions.stream().map(AssignmentSubmission::getScore).filter(s -> s != null).count();
            BigDecimal averageScore = scoredCount == 0 ? BigDecimal.ZERO : avg.divide(BigDecimal.valueOf(scoredCount), 2, RoundingMode.HALF_UP);

            return new LearnerProgressResponse(
                    learner.getId(),
                    learner.getEmail(),
                    learner.getFirstName() + " " + learner.getLastName(),
                    total,
                    submitted,
                    averageScore
            );
        }).toList();
    }

    @Override
    @Transactional
    public AssignmentFeedbackResponse provideFeedback(UUID submissionId, ProvideFeedbackRequest request, User actor) {
        ensureInstructor(actor);
        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Submission not found"));

        Course course = submission.getAssignment().getCourse();
        boolean isCourseInstructor = courseInstructorRepository.findByCourseAndInstructorUser(course, actor).isPresent();
        if (!isCourseInstructor && !isAdmin(actor)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not assigned to this course");
        }

        if (request.score() != null) {
            submission.setScore(request.score());
            submission.setStatus(SubmissionStatus.GRADED);
            assignmentSubmissionRepository.save(submission);
            notifyAssignmentGraded(submission);
        }

        AssignmentFeedback feedback = new AssignmentFeedback();
        feedback.setSubmission(submission);
        feedback.setInstructorUser(actor);
        feedback.setComment(request.comment().trim());
        AssignmentFeedback saved = assignmentFeedbackRepository.save(feedback);
        notifyFeedbackAdded(submission, saved.getComment());

        return new AssignmentFeedbackResponse(
                saved.getId(),
                submission.getId(),
                actor.getId(),
                saved.getComment()
        );
    }

    private void notifyAssignmentCreated(Course course, Assignment assignment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("courseId", course.getId());
        payload.put("courseTitle", course.getTitle());
        payload.put("assignmentId", assignment.getId());
        payload.put("assignmentTitle", assignment.getTitle());
        payload.put("dueAt", assignment.getDueAt());

        Set<UUID> learnerIds = courseEnrollmentRepository.findByCourseAndStatus(course, EnrollmentStatus.ENROLLED).stream()
                .map(enrollment -> enrollment.getUser().getId())
                .collect(Collectors.toSet());
        learnerIds.addAll(courseEnrollmentRepository.findByCourseAndStatus(course, EnrollmentStatus.COMPLETED).stream()
                .map(enrollment -> enrollment.getUser().getId())
                .collect(Collectors.toSet()));

        for (UUID learnerId : learnerIds) {
            notificationService.notifyUser(
                    learnerId,
                    "ASSIGNMENT_CREATED",
                    "New assignment available",
                    "A new assignment was created for " + course.getTitle() + ".",
                    payload
            );
        }
    }

    private void notifyAssignmentGraded(AssignmentSubmission submission) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("submissionId", submission.getId());
        payload.put("assignmentId", submission.getAssignment().getId());
        payload.put("assignmentTitle", submission.getAssignment().getTitle());
        payload.put("courseId", submission.getAssignment().getCourse().getId());
        payload.put("score", submission.getScore());
        payload.put("status", submission.getStatus().name());

        notificationService.notifyUser(
                submission.getLearnerUser().getId(),
                "ASSIGNMENT_GRADED",
                "Assignment graded",
                "Your assignment has been graded.",
                payload
        );
    }

    private void notifyFeedbackAdded(AssignmentSubmission submission, String comment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("submissionId", submission.getId());
        payload.put("assignmentId", submission.getAssignment().getId());
        payload.put("assignmentTitle", submission.getAssignment().getTitle());
        payload.put("courseId", submission.getAssignment().getCourse().getId());
        payload.put("comment", comment);

        notificationService.notifyUser(
                submission.getLearnerUser().getId(),
                "FEEDBACK_ADDED",
                "New instructor feedback",
                "Your submission received instructor feedback.",
                payload
        );
    }

    private Course getCourseAndCheckInstructor(UUID courseId, User actor) {
        ensureInstructor(actor);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
        boolean isCourseInstructor = courseInstructorRepository.findByCourseAndInstructorUser(course, actor).isPresent();
        if (!isCourseInstructor && !isAdmin(actor)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not assigned to this course");
        }
        return course;
    }

    private CourseModule getModuleAndCheckInstructor(UUID moduleId, User actor) {
        ensureInstructor(actor);
        CourseModule module = courseModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Module not found"));
        Course course = module.getCourse();
        boolean isCourseInstructor = courseInstructorRepository.findByCourseAndInstructorUser(course, actor).isPresent();
        if (!isCourseInstructor && !isAdmin(actor)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not assigned to this course");
        }
        return module;
    }

    private Lesson getLessonAndCheckInstructor(UUID lessonId, User actor) {
        ensureInstructor(actor);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson not found"));
        Course course = lesson.getModule().getCourse();
        boolean isCourseInstructor = courseInstructorRepository.findByCourseAndInstructorUser(course, actor).isPresent();
        if (!isCourseInstructor && !isAdmin(actor)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not assigned to this course");
        }
        return lesson;
    }

    private void ensureInstructor(User actor) {
        boolean instructor = actor.getRole() == RoleName.INSTRUCTOR || actor.getRole() == RoleName.ADMIN;
        if (!instructor) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Instructor role required");
        }
    }

    private boolean isAdmin(User actor) {
        return actor.getRole() == RoleName.ADMIN;
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

    private CourseModuleResponse toModuleResponse(CourseModule module, List<Lesson> lessons) {
        return new CourseModuleResponse(
                module.getId(),
                module.getTitle(),
                module.getPosition(),
                lessons.stream().map(this::toLessonResponse).toList()
        );
    }

    private LessonResponse toLessonResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getLessonType().name(),
                lesson.getPosition(),
                lesson.getContentUrl(),
                lesson.getContentText(),
                false // New lessons are not completed yet
        );
    }
}
