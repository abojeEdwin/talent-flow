package com.talentFlow.admin.infrastructure.repository;

import com.talentFlow.admin.domain.Cohort;
import com.talentFlow.admin.domain.ProjectTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectTeamRepository extends JpaRepository<ProjectTeam, UUID> {
    Optional<ProjectTeam> findByCohortAndNameIgnoreCase(Cohort cohort, String name);

    List<ProjectTeam> findByCohortId(UUID cohortId);
}
