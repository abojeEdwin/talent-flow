package com.talentFlow.course.infrastructure.repository;

import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseModuleRepository extends JpaRepository<CourseModule, UUID> {
    List<CourseModule> findByCourseOrderByPositionAsc(Course course);
}
