package com.talentFlow.course.infrastructure.repository;

import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseModule;
import com.talentFlow.course.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByModuleOrderByPositionAsc(CourseModule module);

    List<Lesson> findByModuleInOrderByModule_PositionAscPositionAsc(List<CourseModule> modules);

    long countByModule_Course(Course course);
}
