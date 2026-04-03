package com.talentFlow.instructor.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.common.storage.worker.MediaUploadQueueService;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.common.storage.enums.MediaUploadTargetType;
import com.talentFlow.course.domain.Assignment;
import com.talentFlow.course.domain.AssignmentFeedback;
import com.talentFlow.course.domain.AssignmentSubmission;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseEnrollment;
import com.talentFlow.course.domain.CourseInstructor;
import com.talentFlow.course.domain.CourseMaterial;
import com.talentFlow.course.domain.enums.CourseStatus;
import com.talentFlow.course.domain.enums.EnrollmentStatus;
import com.talentFlow.course.domain.enums.MaterialType;
import com.talentFlow.course.domain.enums.MaterialUploadStatus;
import com.talentFlow.course.domain.enums.SubmissionStatus;
import com.talentFlow.course.infrastructure.repository.AssignmentFeedbackRepository;
import com.talentFlow.course.infrastructure.repository.AssignmentRepository;
import com.talentFlow.course.infrastructure.repository.AssignmentSubmissionRepository;
import com.talentFlow.course.infrastructure.repository.CourseEnrollmentRepository;
import com.talentFlow.course.infrastructure.repository.CourseInstructorRepository;
import com.talentFlow.course.infrastructure.repository.CourseMaterialRepository;
import com.talentFlow.course.infrastructure.repository.CourseRepository;
import com.talentFlow.course.web.dto.AssignmentFeedbackResponse;
import com.talentFlow.course.web.dto.AssignmentResponse;
import com.talentFlow.course.web.dto.CourseMaterialResponse;
import com.talentFlow.course.web.dto.CourseResponse;
import com.talentFlow.course.web.dto.CreateAssignmentRequest;
import com.talentFlow.course.web.dto.CreateCourseRequest;
import com.talentFlow.course.web.dto.LearnerProgressResponse;
import com.talentFlow.course.web.dto.ProvideFeedbackRequest;
import com.talentFlow.course.web.dto.UploadMaterialRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorServiceImpl implements InstructorService {

    private final CourseRepository courseRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseMaterialRepository courseMaterialRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final AssignmentFeedbackRepository assignmentFeedbackRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final MediaUploadQueueService mediaUploadQueueService;

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
                    "courses/cover-images",
                    coverImage
            );
        }
        if (introVideo != null && !introVideo.isEmpty()) {
            mediaUploadQueueService.enqueue(
                    MediaUploadTargetType.COURSE_INTRO_VIDEO,
                    saved.getId(),
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
    public CourseMaterialResponse uploadMaterial(UUID courseId, UploadMaterialRequest request, User actor) {
        Course course = getCourseAndCheckInstructor(courseId, actor);
        CourseMaterial material = new CourseMaterial();
        material.setCourse(course);
        material.setTitle(request.title().trim());
        material.setMaterialType(request.materialType());
        material.setContentUrl(request.contentUrl().trim());
        material.setUploadStatus(MaterialUploadStatus.COMPLETED);
        material.setUploadedByUser(actor);
        CourseMaterial saved = courseMaterialRepository.save(material);
        return new CourseMaterialResponse(
                saved.getId(),
                course.getId(),
                saved.getTitle(),
                saved.getMaterialType().name(),
                saved.getContentUrl(),
                saved.getUploadStatus().name(),
                actor.getId()
        );
    }

    @Override
    @Transactional
    public CourseMaterialResponse uploadMaterialFile(UUID courseId,
                                                     String title,
                                                     MaterialType materialType,
                                                     MultipartFile file,
                                                     User actor) {
        if (title == null || title.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Material title is required");
        }
        if (materialType == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Material type is required");
        }
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Material file is required");
        }

        Course course = getCourseAndCheckInstructor(courseId, actor);

        CourseMaterial material = new CourseMaterial();
        material.setCourse(course);
        material.setTitle(title.trim());
        material.setMaterialType(materialType);
        material.setContentUrl(null);
        material.setUploadStatus(MaterialUploadStatus.PENDING);
        material.setUploadedByUser(actor);
        CourseMaterial saved = courseMaterialRepository.save(material);

        mediaUploadQueueService.enqueue(
                MediaUploadTargetType.COURSE_MATERIAL,
                saved.getId(),
                "courses/" + courseId + "/materials",
                file
        );

        return new CourseMaterialResponse(
                saved.getId(),
                course.getId(),
                saved.getTitle(),
                saved.getMaterialType().name(),
                saved.getContentUrl(),
                saved.getUploadStatus().name(),
                actor.getId()
        );
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
        }

        AssignmentFeedback feedback = new AssignmentFeedback();
        feedback.setSubmission(submission);
        feedback.setInstructorUser(actor);
        feedback.setComment(request.comment().trim());
        AssignmentFeedback saved = assignmentFeedbackRepository.save(feedback);

        return new AssignmentFeedbackResponse(
                saved.getId(),
                submission.getId(),
                actor.getId(),
                saved.getComment()
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
}
