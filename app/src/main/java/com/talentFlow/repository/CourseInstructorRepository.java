package com.talentFlow.repository;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.entity.Course;
import com.talentFlow.data.entity.CourseInstructor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseInstructorRepository extends MongoRepository<CourseInstructor, UUID> {
    List<CourseInstructor> findByCourse(Course course);

    List<CourseInstructor> findByInstructorUser(User user);

    Optional<CourseInstructor> findByCourseAndInstructorUser(Course course, User instructorUser);

    void deleteByCourse(Course course);
}