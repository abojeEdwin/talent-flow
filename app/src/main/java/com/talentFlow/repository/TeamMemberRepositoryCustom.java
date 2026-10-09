package com.talentFlow.repository;

import com.talentFlow.data.entity.TeamMember;

import java.util.List;
import java.util.UUID;

public interface TeamMemberRepositoryCustom {
    List<TeamMember> findByTeamCohortId(UUID cohortId);
}