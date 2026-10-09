package com.talentFlow.repository;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.entity.Lesson;
import com.talentFlow.data.entity.LessonProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonProgressRepository extends MongoRepository<LessonProgress, UUID> {
    Optional<LessonProgress> findByUserAndLesson(User user, Lesson lesson);

    long countByUserAndCompletedTrueAndLessonIdIn(User user, List<UUID> lessonIds);

    List<LessonProgress> findByUserAndLessonIn(User user, List<Lesson> lessons);
}