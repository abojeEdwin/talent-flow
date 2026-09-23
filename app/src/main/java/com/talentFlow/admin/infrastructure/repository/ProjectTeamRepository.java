package com.talentFlow.admin.infrastructure.repository;

import com.talentFlow.admin.domain.ProjectTeam;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectTeamRepository extends MongoRepository<ProjectTeam, UUID> {
    List<ProjectTeam> findByCohortId(UUID cohortId);

    Optional<ProjectTeam> findByCohortIdAndNameIgnoreCase(UUID cohortId, String name);
}