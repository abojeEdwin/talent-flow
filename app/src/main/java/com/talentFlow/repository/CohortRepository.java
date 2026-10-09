package com.talentFlow.repository;

import com.talentFlow.data.entity.Cohort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface CohortRepository extends MongoRepository<Cohort, UUID> {
    List<Cohort> findByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}