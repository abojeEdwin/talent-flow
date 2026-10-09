package com.talentFlow.repository;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.entity.Course;
import com.talentFlow.data.Enums.CourseStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends MongoRepository<Course, UUID> {
    List<Course> findByStatus(CourseStatus status);

    List<Course> findByCreatedByUser(User user);

    Optional<Course> findByTitleIgnoreCase(String title);
}