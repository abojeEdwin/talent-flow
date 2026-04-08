package com.talentFlow.admin.application;

import com.talentFlow.admin.domain.AdminAuditLog;
import com.talentFlow.admin.domain.Cohort;
import com.talentFlow.admin.domain.ProjectTeam;
import com.talentFlow.admin.domain.TeamMember;
import com.talentFlow.admin.domain.TeamMemberId;
import com.talentFlow.admin.infrastructure.repository.AdminAuditLogRepository;
import com.talentFlow.admin.infrastructure.repository.CohortRepository;
import com.talentFlow.admin.infrastructure.repository.ProjectTeamRepository;
import com.talentFlow.admin.infrastructure.repository.TeamMemberRepository;
import com.talentFlow.admin.web.dto.AllocateUserToTeamRequest;
import com.talentFlow.admin.web.dto.CohortResponse;
import com.talentFlow.admin.web.dto.CreateCohortRequest;
import com.talentFlow.admin.web.dto.CreateProjectTeamRequest;
import com.talentFlow.admin.web.dto.ProjectTeamResponse;
import com.talentFlow.admin.web.dto.TeamMemberResponse;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminProgramServiceImpl implements AdminProgramService {

    private final CohortRepository cohortRepository;
    private final ProjectTeamRepository projectTeamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    @Override
    @Transactional
    public CohortResponse createCohort(CreateCohortRequest request, User actor) {
        if (cohortRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "Cohort name already exists");
        }
        if (request.endDate().isBefore(request.startDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cohort endDate cannot be before startDate");
        }

        Cohort cohort = new Cohort();
        cohort.setName(request.name().trim());
        cohort.setDescription(request.description());
        cohort.setIntakeYear(request.intakeYear());
        cohort.setStartDate(request.startDate());
        cohort.setEndDate(request.endDate());
        cohort.setActive(true);
        Cohort saved = cohortRepository.save(cohort);

        writeAudit(actor, "COHORT_CREATED", "COHORT", saved.getId(), "Created cohort " + saved.getName());
        return toCohortResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CohortResponse> listAllCohorts() {
        return cohortRepository.findAll().stream().map(this::toCohortResponse).toList();
    }

    @Override
    @Transactional
    public ProjectTeamResponse createProjectTeam(CreateProjectTeamRequest request, User actor) {
        Cohort cohort = cohortRepository.findById(request.cohortId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cohort not found"));
        projectTeamRepository.findByCohortAndNameIgnoreCase(cohort, request.name().trim()).ifPresent(t -> {
            throw new ApiException(HttpStatus.CONFLICT, "Team name already exists in this cohort");
        });

        ProjectTeam team = new ProjectTeam();
        team.setCohort(cohort);
        team.setName(request.name().trim());
        team.setDescription(request.description());
        ProjectTeam saved = projectTeamRepository.save(team);

        writeAudit(actor, "TEAM_CREATED", "TEAM", saved.getId(), "Created team " + saved.getName());
        return toTeamResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectTeamResponse> listAllProjectTeams() {
        return projectTeamRepository.findAll().stream().map(this::toTeamResponse).toList();
    }

    @Override
    @Transactional
    public TeamMemberResponse allocateUserToTeam(UUID teamId, AllocateUserToTeamRequest request, User actor) {
        ProjectTeam team = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        TeamMemberId id = new TeamMemberId(team.getId(), user.getId());
        TeamMember member = teamMemberRepository.findById(id).orElseGet(TeamMember::new);
        member.setId(id);
        member.setTeam(team);
        member.setUser(user);
        member.setTeamRole(request.teamRole().trim());

        TeamMember saved = teamMemberRepository.save(member);
        writeAudit(actor, "TEAM_MEMBER_ALLOCATED", "TEAM", team.getId(),
                "Allocated user " + user.getEmail() + " as " + saved.getTeamRole());

        return new TeamMemberResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                saved.getTeamRole()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectTeamResponse> listCohortTeams(UUID cohortId) {
        return projectTeamRepository.findByCohortId(cohortId).stream().map(this::toTeamResponse).toList();
    }

    private CohortResponse toCohortResponse(Cohort cohort) {
        return new CohortResponse(
                cohort.getId(),
                cohort.getName(),
                cohort.getDescription(),
                cohort.getIntakeYear(),
                cohort.getStartDate(),
                cohort.getEndDate(),
                cohort.isActive()
        );
    }

    private ProjectTeamResponse toTeamResponse(ProjectTeam team) {
        return new ProjectTeamResponse(
                team.getId(),
                team.getCohort().getId(),
                team.getName(),
                team.getDescription()
        );
    }

    private void writeAudit(User actor, String action, String resourceType, UUID resourceId, String details) {
        AdminAuditLog auditLog = new AdminAuditLog();
        auditLog.setActorUser(actor);
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setDetails(details);
        adminAuditLogRepository.save(auditLog);
    }
}
