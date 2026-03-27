package com.talentFlow.admin.infrastructure.repository;

import com.talentFlow.admin.domain.Cohort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CohortRepository extends JpaRepository<Cohort, UUID> {
    boolean existsByNameIgnoreCase(String name);
}
