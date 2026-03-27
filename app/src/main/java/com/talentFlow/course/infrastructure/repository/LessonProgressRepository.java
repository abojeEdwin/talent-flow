package com.talentFlow.course.infrastructure.repository;

import com.talentFlow.auth.domain.User;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.Lesson;
import com.talentFlow.course.domain.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {
    Optional<LessonProgress> findByUserAndLesson(User user, Lesson lesson);

    long countByUserAndCompletedTrueAndLesson_Module_Course(User user, Course course);

    List<LessonProgress> findByUserAndLessonIn(User user, List<Lesson> lessons);
}
