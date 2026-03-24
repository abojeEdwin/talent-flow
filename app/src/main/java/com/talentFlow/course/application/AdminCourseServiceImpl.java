package com.talentFlow.course.application;

import com.talentFlow.admin.domain.AdminAuditLog;
import com.talentFlow.admin.domain.TeamMember;
import com.talentFlow.admin.infrastructure.repository.AdminAuditLogRepository;
import com.talentFlow.admin.infrastructure.repository.TeamMemberRepository;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
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

@Service
@RequiredArgsConstructor
public class AdminCourseServiceImpl implements AdminCourseService {

    private final CourseRepository courseRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> listCourses(CourseStatus status) {
        List<Course> courses = status == null ? courseRepository.findAll() : courseRepository.findByStatus(status);
        return courses.stream().map(this::toCourseResponse).toList();
    }

    @Override
    @Transactional
    public CourseResponse publishCourse(UUID courseId, User actor) {
        Course course = getCourse(courseId);
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Archived course cannot be published");
        }
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        Course saved = courseRepository.save(course);
        audit(actor, "COURSE_PUBLISHED", "COURSE", saved.getId(), "Published course");
        return toCourseResponse(saved);
    }

    @Override
    @Transactional
    public CourseResponse unpublishCourse(UUID courseId, User actor) {
        Course course = getCourse(courseId);
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Archived course cannot be unpublished");
        }
        course.setStatus(CourseStatus.DRAFT);
        course.setPublishedAt(null);
        Course saved = courseRepository.save(course);
        audit(actor, "COURSE_UNPUBLISHED", "COURSE", saved.getId(), "Unpublished course");
        return toCourseResponse(saved);
    }

    @Override
    @Transactional
    public CourseResponse archiveCourse(UUID courseId, User actor) {
        Course course = getCourse(courseId);
        course.setStatus(CourseStatus.ARCHIVED);
        course.setArchivedAt(LocalDateTime.now());
        Course saved = courseRepository.save(course);
        audit(actor, "COURSE_ARCHIVED", "COURSE", saved.getId(), "Archived course");
        return toCourseResponse(saved);
    }

    @Override
    @Transactional
    public CourseResponse assignInstructors(UUID courseId, AssignInstructorsRequest request, User actor) {
        Course course = getCourse(courseId);
        User primaryInstructor = getMentorUser(request.primaryInstructorId());

        Set<UUID> allInstructorIds = new HashSet<>();
        allInstructorIds.add(primaryInstructor.getId());
        if (request.coInstructorIds() != null) {
            allInstructorIds.addAll(request.coInstructorIds());
        }

        courseInstructorRepository.deleteByCourse(course);
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
            enrolled += enrollOne(course, user);
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
            enrolled += enrollOne(course, user);
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

        audit(actor, "COURSE_ENROLLMENT_REVOKED", "COURSE", course.getId(),
                "Revoked user " + user.getEmail());
    }

    private int enrollOne(Course course, User user) {
        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseAndUser(course, user).orElse(null);
        if (enrollment == null) {
            enrollment = new CourseEnrollment();
            enrollment.setCourse(course);
            enrollment.setUser(user);
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setProgressPct(BigDecimal.ZERO);
        }
        if (enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
            return 0;
        }
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setCompletedAt(null);
        enrollment.setRevokedAt(null);
        courseEnrollmentRepository.save(enrollment);
        return 1;
    }

    private Course getCourse(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
    }

    private User getMentorUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        boolean mentor = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.MENTOR || r.getName() == RoleName.ADMIN);
        if (!mentor) {
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
}
