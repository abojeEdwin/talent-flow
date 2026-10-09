package com.talentFlow.repository;

import com.talentFlow.data.entity.Assignment;
import com.talentFlow.data.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepository extends MongoRepository<Assignment, UUID> {
    List<Assignment> findByCourse(Course course);

    Page<Assignment> findByCourseIn(List<Course> courses, Pageable pageable);

    Optional<Assignment> findByCourseAndTitleIgnoreCase(Course course, String title);
}