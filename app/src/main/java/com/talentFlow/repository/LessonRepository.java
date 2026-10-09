package com.talentFlow.repository;

import com.talentFlow.data.entity.CourseModule;
import com.talentFlow.data.entity.Lesson;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends MongoRepository<Lesson, UUID> {
    List<Lesson> findByModuleOrderByPositionAsc(CourseModule module);

    List<Lesson> findByModuleInOrderByPositionAsc(List<CourseModule> modules);

    long countByModuleIdIn(List<UUID> moduleIds);

    boolean existsByModule(CourseModule module);

    Optional<Lesson> findByModuleAndTitleIgnoreCase(CourseModule module, String title);
}