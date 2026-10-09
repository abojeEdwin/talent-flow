package com.talentFlow.service;

import com.talentFlow.data.dto.AllocateUserToTeamRequest;
import com.talentFlow.data.dto.AutoAllocateTeamMembersResponse;
import com.talentFlow.data.dto.CohortResponse;
import com.talentFlow.data.dto.CreateCohortRequest;
import com.talentFlow.data.dto.CreateProjectTeamRequest;
import com.talentFlow.data.dto.ProjectTeamResponse;
import com.talentFlow.data.dto.TeamMemberResponse;
import com.talentFlow.auth.data.entity.User;

import java.util.List;
import java.util.UUID;

public interface AdminProgramService {
    CohortResponse createCohort(CreateCohortRequest request, User actor);

    List<CohortResponse> listAllCohorts();

    ProjectTeamResponse createProjectTeam(CreateProjectTeamRequest request, User actor);

    List<ProjectTeamResponse> listAllProjectTeams();

    TeamMemberResponse allocateUserToTeam(UUID teamId, AllocateUserToTeamRequest request, User actor);

    AutoAllocateTeamMembersResponse autoAllocateUnallocatedInterns(UUID teamId, User actor);

    List<TeamMemberResponse> listTeamMembers(UUID teamId);

    List<ProjectTeamResponse> listCohortTeams(UUID cohortId);

    List<TeamMemberResponse> listAllAllocatedInterns();
}
