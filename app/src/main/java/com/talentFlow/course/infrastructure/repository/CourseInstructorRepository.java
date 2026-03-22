package com.talentFlow.course.infrastructure.repository;

import com.talentFlow.auth.domain.User;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseInstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseInstructorRepository extends JpaRepository<CourseInstructor, UUID> {
    List<CourseInstructor> findByCourse(Course course);

    List<CourseInstructor> findByInstructorUser(User user);

    Optional<CourseInstructor> findByCourseAndInstructorUser(Course course, User instructorUser);

    void deleteByCourse(Course course);
}
