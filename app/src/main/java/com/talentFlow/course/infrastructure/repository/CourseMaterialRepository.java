package com.talentFlow.course.infrastructure.repository;

import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseMaterial;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface CourseMaterialRepository extends MongoRepository<CourseMaterial, UUID> {
    List<CourseMaterial> findByCourse(Course course);
}