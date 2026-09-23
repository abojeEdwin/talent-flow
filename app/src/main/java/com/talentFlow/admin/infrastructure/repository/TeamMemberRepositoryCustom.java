package com.talentFlow.admin.infrastructure.repository;

import com.talentFlow.admin.domain.TeamMember;

import java.util.List;
import java.util.UUID;

public interface TeamMemberRepositoryCustom {
    List<TeamMember> findByTeamCohortId(UUID cohortId);
}