package com.talentFlow.repository;

import com.talentFlow.data.entity.Course;
import com.talentFlow.data.entity.CourseMaterial;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface CourseMaterialRepository extends MongoRepository<CourseMaterial, UUID> {
    List<CourseMaterial> findByCourse(Course course);
}