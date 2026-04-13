package com.talentFlow.course.infrastructure.repository;

import com.talentFlow.auth.domain.User;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseEnrollment;
import com.talentFlow.course.domain.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, UUID> {
    Optional<CourseEnrollment> findByCourseAndUser(Course course, User user);

    List<CourseEnrollment> findByCourseAndStatus(Course course, EnrollmentStatus status);

    List<CourseEnrollment> findByUserAndStatus(User user, EnrollmentStatus status);
    List<CourseEnrollment> findByUser(User user);

}
