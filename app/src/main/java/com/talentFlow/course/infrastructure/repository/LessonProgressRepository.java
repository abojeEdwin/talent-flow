package com.talentFlow.course.infrastructure.repository;

import com.talentFlow.auth.domain.User;
import com.talentFlow.course.domain.Lesson;
import com.talentFlow.course.domain.LessonProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonProgressRepository extends MongoRepository<LessonProgress, UUID> {
    Optional<LessonProgress> findByUserAndLesson(User user, Lesson lesson);

    long countByUserAndCompletedTrueAndLessonIdIn(User user, List<UUID> lessonIds);

    List<LessonProgress> findByUserAndLessonIn(User user, List<Lesson> lessons);
}