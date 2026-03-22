package com.talentFlow.learner.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseEnrollment;
import com.talentFlow.course.domain.enums.CourseStatus;
import com.talentFlow.course.domain.enums.EnrollmentStatus;
import com.talentFlow.course.infrastructure.repository.CourseEnrollmentRepository;
import com.talentFlow.course.infrastructure.repository.CourseInstructorRepository;
import com.talentFlow.course.infrastructure.repository.CourseRepository;
import com.talentFlow.course.web.dto.CourseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearnerCourseServiceImpl implements LearnerCourseService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;

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
        enrollment.setRevokedAt(null);
        courseEnrollmentRepository.save(enrollment);
        return toCourseResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> myEnrollments(User learner) {
        return courseEnrollmentRepository.findAll().stream()
                .filter(enrollment -> enrollment.getUser().getId().equals(learner.getId()))
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED)
                .map(CourseEnrollment::getCourse)
                .distinct()
                .map(this::toCourseResponse)
                .toList();
    }

    private CourseResponse toCourseResponse(Course course) {
        Set<UUID> instructorIds = courseInstructorRepository.findByCourse(course).stream()
                .map(ci -> ci.getInstructorUser().getId())
                .collect(Collectors.toSet());
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getStatus().name(),
                course.getPublishedAt(),
                course.getArchivedAt(),
                course.getCreatedByUser().getId(),
                instructorIds
        );
    }
}
