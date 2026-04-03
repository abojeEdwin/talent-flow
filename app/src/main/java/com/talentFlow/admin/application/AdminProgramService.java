package com.talentFlow.admin.application;

import com.talentFlow.admin.web.dto.AllocateUserToTeamRequest;
import com.talentFlow.admin.web.dto.CohortResponse;
import com.talentFlow.admin.web.dto.CreateCohortRequest;
import com.talentFlow.admin.web.dto.CreateProjectTeamRequest;
import com.talentFlow.admin.web.dto.ProjectTeamResponse;
import com.talentFlow.admin.web.dto.TeamMemberResponse;
import com.talentFlow.auth.domain.User;

import java.util.List;
import java.util.UUID;

public interface AdminProgramService {
    CohortResponse createCohort(CreateCohortRequest request, User actor);

    List<CohortResponse> listAllCohorts();

    ProjectTeamResponse createProjectTeam(CreateProjectTeamRequest request, User actor);

    TeamMemberResponse allocateUserToTeam(UUID teamId, AllocateUserToTeamRequest request, User actor);

    List<ProjectTeamResponse> listCohortTeams(UUID cohortId);
}
