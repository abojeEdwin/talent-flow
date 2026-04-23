package com.talentFlow.course.application;

import com.talentFlow.admin.domain.AdminAuditLog;
import com.talentFlow.admin.domain.TeamMember;
import com.talentFlow.admin.infrastructure.repository.AdminAuditLogRepository;
import com.talentFlow.admin.infrastructure.repository.TeamMemberRepository;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.notification.application.NotificationService;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseEnrollment;
import com.talentFlow.course.domain.CourseInstructor;
import com.talentFlow.course.domain.enums.CourseStatus;
import com.talentFlow.course.domain.enums.EnrollmentStatus;
import com.talentFlow.course.infrastructure.repository.CourseEnrollmentRepository;
import com.talentFlow.course.infrastructure.repository.CourseInstructorRepository;
import com.talentFlow.course.infrastructure.repository.CourseRepository;
import com.talentFlow.course.web.dto.AssignInstructorsRequest;
import com.talentFlow.course.web.dto.CourseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AdminCourseServiceImpl implements AdminCourseService {

    //TODO:Implement cache-aside pattern

    private final CourseRepository courseRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "courses", key = "#status?.name() ?: 'all'")
    public List<CourseResponse> listCourses(CourseStatus status) {
        List<Course> courses = status == null ? courseRepository.findAll() : courseRepository.findByStatus(status);
        return courses.stream().map(this::toCourseResponse).toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponse publishCourse(UUID courseId, User actor) {
        Course course = getCourse(courseId);
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Archived course cannot be published");
        }
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        Course saved = courseRepository.save(course);
        audit(actor, "COURSE_PUBLISHED", "COURSE", saved.getId(), "Published course");
        notifyCourseStatusChange(saved, "COURSE_PUBLISHED", "Course published", "A course is now available for learning.");
        return toCourseResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponse unpublishCourse(UUID courseId, User actor) {
        Course course = getCourse(courseId);
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Archived course cannot be unpublished");
        }
        course.setStatus(CourseStatus.DRAFT);
        course.setPublishedAt(null);
        Course saved = courseRepository.save(course);
        audit(actor, "COURSE_UNPUBLISHED", "COURSE", saved.getId(), "Unpublished course");
        notifyCourseStatusChange(saved, "COURSE_UNPUBLISHED", "Course unpublished", "A course has been moved back to draft.");
        return toCourseResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponse archiveCourse(UUID courseId, User actor) {
        Course course = getCourse(courseId);
        course.setStatus(CourseStatus.ARCHIVED);
        course.setArchivedAt(LocalDateTime.now());
        Course saved = courseRepository.save(course);
        audit(actor, "COURSE_ARCHIVED", "COURSE", saved.getId(), "Archived course");
        notifyCourseStatusChange(saved, "COURSE_ARCHIVED", "Course archived", "A course has been archived.");
        return toCourseResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponse assignInstructors(UUID courseId, AssignInstructorsRequest request, User actor) {
        Course course = getCourse(courseId);
        User primaryInstructor = getMentorUser(request.primaryInstructorId());

        Set<UUID> allInstructorIds = new HashSet<>();
        allInstructorIds.add(primaryInstructor.getId());
        if (request.coInstructorIds() != null) {
            allInstructorIds.addAll(request.coInstructorIds());
        }

        Set<UUID> previousInstructorIds = courseInstructorRepository.findByCourse(course).stream()
                .map(ci -> ci.getInstructorUser().getId())
                .collect(Collectors.toSet());

        courseInstructorRepository.deleteByCourse(course);
        courseInstructorRepository.flush();

        for (UUID instructorId : allInstructorIds) {
            User instructor = getMentorUser(instructorId);
            CourseInstructor courseInstructor = new CourseInstructor();
            courseInstructor.setCourse(course);
            courseInstructor.setInstructorUser(instructor);
            courseInstructor.setPrimary(instructor.getId().equals(primaryInstructor.getId()));
            courseInstructorRepository.save(courseInstructor);
        }

        audit(actor, "COURSE_INSTRUCTORS_ASSIGNED", "COURSE", course.getId(),
                "Assigned instructors: " + allInstructorIds);

        notifyInstructorAssignments(course, previousInstructorIds, allInstructorIds);
        return toCourseResponse(course);
    }

    @Override
    @Transactional
    public int bulkEnrollCohort(UUID courseId, UUID cohortId, User actor) {
        Course course = getCourse(courseId);
        Set<User> users = teamMemberRepository.findByTeam_Cohort_Id(cohortId).stream()
                .map(TeamMember::getUser)
                .collect(Collectors.toSet());
        int enrolled = 0;
        for (User user : users) {
            boolean changed = enrollOne(course, user);
            if (changed) {
                enrolled += 1;
                notifyEnrollmentGranted(course, user, "You have been enrolled in a course by your cohort.");
            }
        }
        audit(actor, "COURSE_BULK_ENROLL_COHORT", "COURSE", course.getId(),
                "Bulk enrolled cohort " + cohortId + ", count=" + enrolled);
        return enrolled;
    }

    @Override
    @Transactional
    public int bulkEnrollTeam(UUID courseId, UUID teamId, User actor) {
        Course course = getCourse(courseId);
        Set<User> users = teamMemberRepository.findByTeam_Id(teamId).stream()
                .map(TeamMember::getUser)
                .collect(Collectors.toSet());
        int enrolled = 0;
        for (User user : users) {
            boolean changed = enrollOne(course, user);
            if (changed) {
                enrolled += 1;
                notifyEnrollmentGranted(course, user, "You have been enrolled in a course by your team.");
            }
        }
        audit(actor, "COURSE_BULK_ENROLL_TEAM", "COURSE", course.getId(),
                "Bulk enrolled team " + teamId + ", count=" + enrolled);
        return enrolled;
    }

    @Override
    @Transactional
    public void revokeEnrollment(UUID courseId, UUID userId, User actor) {
        Course course = getCourse(courseId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseAndUser(course, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enrollment not found"));

        enrollment.setStatus(EnrollmentStatus.REVOKED);
        enrollment.setRevokedAt(LocalDateTime.now());
        enrollment.setCompletedAt(null);
        courseEnrollmentRepository.save(enrollment);
        notifyEnrollmentRevoked(course, user);

        audit(actor, "COURSE_ENROLLMENT_REVOKED", "COURSE", course.getId(),
                "Revoked user " + user.getEmail());
    }

    private boolean enrollOne(Course course, User user) {
        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseAndUser(course, user).orElse(null);
        if (enrollment == null) {
            enrollment = new CourseEnrollment();
            enrollment.setCourse(course);
            enrollment.setUser(user);
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setProgressPct(BigDecimal.ZERO);
        }
        if (enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
            return false;
        }
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setCompletedAt(null);
        enrollment.setRevokedAt(null);
        courseEnrollmentRepository.save(enrollment);
        return true;
    }

    private Course getCourse(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
    }

    private User getMentorUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        boolean instructor = user.getRole() == RoleName.INSTRUCTOR || user.getRole() == RoleName.ADMIN;
        if (!instructor) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User is not an instructor");
        }
        return user;
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

    private void audit(User actor, String action, String resourceType, UUID resourceId, String details) {
        AdminAuditLog audit = new AdminAuditLog();
        audit.setActorUser(actor);
        audit.setAction(action);
        audit.setResourceType(resourceType);
        audit.setResourceId(resourceId);
        audit.setDetails(details);
        adminAuditLogRepository.save(audit);
    }

    private void notifyCourseStatusChange(Course course, String type, String title, String message) {
        Set<UUID> recipientIds = new HashSet<>();
        recipientIds.addAll(courseInstructorRepository.findByCourse(course).stream()
                .map(ci -> ci.getInstructorUser().getId())
                .collect(Collectors.toSet()));
        recipientIds.addAll(courseEnrollmentRepository.findByCourseAndStatus(course, EnrollmentStatus.ENROLLED).stream()
                .map(enrollment -> enrollment.getUser().getId())
                .collect(Collectors.toSet()));
        recipientIds.addAll(courseEnrollmentRepository.findByCourseAndStatus(course, EnrollmentStatus.COMPLETED).stream()
                .map(enrollment -> enrollment.getUser().getId())
                .collect(Collectors.toSet()));

        Map<String, Object> payload = new HashMap<>();
        payload.put("courseId", course.getId());
        payload.put("courseTitle", course.getTitle());
        payload.put("status", course.getStatus().name());

        for (UUID userId : recipientIds) {
            notificationService.notifyUser(userId, type, title, message, payload);
        }
    }

    private void notifyInstructorAssignments(Course course, Set<UUID> previousInstructorIds, Set<UUID> currentInstructorIds) {
        Set<UUID> added = new HashSet<>(currentInstructorIds);
        added.removeAll(previousInstructorIds);

        Set<UUID> removed = new HashSet<>(previousInstructorIds);
        removed.removeAll(currentInstructorIds);

        Map<String, Object> payload = new HashMap<>();
        payload.put("courseId", course.getId());
        payload.put("courseTitle", course.getTitle());
        payload.put("instructorIds", currentInstructorIds);

        for (UUID instructorId : added) {
            notificationService.notifyUser(
                    instructorId,
                    "COURSE_INSTRUCTORS_ASSIGNED",
                    "Course assignment updated",
                    "You have been assigned to teach a course.",
                    payload
            );
        }

        for (UUID instructorId : removed) {
            notificationService.notifyUser(
                    instructorId,
                    "COURSE_INSTRUCTORS_UNASSIGNED",
                    "Course assignment updated",
                    "You have been removed from a course.",
                    payload
            );
        }
    }

    private void notifyEnrollmentGranted(Course course, User user, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("courseId", course.getId());
        payload.put("courseTitle", course.getTitle());
        payload.put("status", EnrollmentStatus.ENROLLED.name());

        notificationService.notifyUser(
                user.getId(),
                "ENROLLMENT_GRANTED",
                "Course enrollment granted",
                message,
                payload
        );
    }

    private void notifyEnrollmentRevoked(Course course, User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("courseId", course.getId());
        payload.put("courseTitle", course.getTitle());
        payload.put("status", EnrollmentStatus.REVOKED.name());

        notificationService.notifyUser(
                user.getId(),
                "ENROLLMENT_REVOKED",
                "Course enrollment revoked",
                "Your enrollment for this course has been revoked.",
                payload
        );
    }
}
