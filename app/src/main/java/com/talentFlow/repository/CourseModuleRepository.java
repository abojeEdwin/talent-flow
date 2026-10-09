package com.talentFlow.repository;

import com.talentFlow.data.entity.Course;
import com.talentFlow.data.entity.CourseModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseModuleRepository extends MongoRepository<CourseModule, UUID> {
    List<CourseModule> findByCourseOrderByPositionAsc(Course course);

    Page<CourseModule> findByCourseOrderByPositionAsc(Course course, Pageable pageable);

    Optional<CourseModule> findByCourseAndTitleIgnoreCase(Course course, String title);
}