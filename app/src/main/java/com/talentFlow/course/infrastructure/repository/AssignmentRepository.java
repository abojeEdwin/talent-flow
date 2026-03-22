package com.talentFlow.course.infrastructure.repository;

import com.talentFlow.course.domain.Assignment;
import com.talentFlow.course.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    List<Assignment> findByCourse(Course course);
}
