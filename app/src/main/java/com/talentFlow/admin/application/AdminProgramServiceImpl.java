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
import com.talentFlow.admin.web.dto.AutoAllocateTeamMembersResponse;
import com.talentFlow.admin.web.dto.CohortResponse;
import com.talentFlow.admin.web.dto.CreateCohortRequest;
import com.talentFlow.admin.web.dto.CreateProjectTeamRequest;
import com.talentFlow.admin.web.dto.ProjectTeamResponse;
import com.talentFlow.admin.web.dto.TeamMemberResponse;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.notification.application.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminProgramServiceImpl implements AdminProgramService {
    private static final int MAX_TEAM_SIZE = 10;

    private final CohortRepository cohortRepository;
    private final ProjectTeamRepository projectTeamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @CacheEvict(value = "cohorts", allEntries = true)
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
    @Cacheable(value = "cohorts")
    public List<CohortResponse> listAllCohorts() {
        return cohortRepository.findAll().stream().map(this::toCohortResponse).toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "teams", allEntries = true),
            @CacheEvict(value = "cohort_teams", key = "#request.cohortId()")
    })
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
    @Cacheable(value = "teams")
    public List<ProjectTeamResponse> listAllProjectTeams() {
        return projectTeamRepository.findAll().stream().map(this::toTeamResponse).toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "team_members", key = "#teamId"),
            @CacheEvict(value = "allocated_interns", allEntries = true)
    })
    public TeamMemberResponse allocateUserToTeam(UUID teamId, AllocateUserToTeamRequest request, User actor) {
        ProjectTeam team = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        TeamMemberId id = new TeamMemberId(team.getId(), user.getId());
        boolean alreadyMember = teamMemberRepository.existsById(id);
        long currentTeamSize = teamMemberRepository.countByTeam_Id(teamId);
        if (!alreadyMember && currentTeamSize >= MAX_TEAM_SIZE) {
            throw new ApiException(HttpStatus.CONFLICT, "Team has reached max capacity of " + MAX_TEAM_SIZE);
        }

        TeamMember member = teamMemberRepository.findById(id).orElseGet(TeamMember::new);
        member.setId(id);
        member.setTeam(team);
        member.setUser(user);
        member.setTeamRole(request.teamRole().trim());

        TeamMember saved = teamMemberRepository.save(member);
        writeAudit(actor, "TEAM_MEMBER_ALLOCATED", "TEAM", team.getId(),
                "Allocated user " + user.getEmail() + " as " + saved.getTeamRole());
        notifyTeamAllocated(team, user, saved.getTeamRole(), false);

        return new TeamMemberResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                saved.getTeamRole()
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "team_members", key = "#teamId"),
            @CacheEvict(value = "allocated_interns", allEntries = true)
    })
    public AutoAllocateTeamMembersResponse autoAllocateUnallocatedInterns(UUID teamId, User actor) {
        ProjectTeam team = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));

        long currentTeamSize = teamMemberRepository.countByTeam_Id(teamId);
        if (currentTeamSize > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Team already has members; auto-allocation only supports empty teams");
        }
        if (currentTeamSize >= MAX_TEAM_SIZE) {
            throw new ApiException(HttpStatus.CONFLICT, "Team has reached max capacity of " + MAX_TEAM_SIZE);
        }

        int slotsToFill = (int) (MAX_TEAM_SIZE - currentTeamSize);
        List<User> unallocatedInterns = userRepository.findUnallocatedInterns(
                RoleName.INTERN,
                PageRequest.of(0, slotsToFill)
        ).getContent();

        if (unallocatedInterns.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "No unallocated interns available");
        }

        List<TeamMemberResponse> allocatedMembers = new ArrayList<>();
        for (User intern : unallocatedInterns) {
            TeamMember member = new TeamMember();
            member.setId(new TeamMemberId(team.getId(), intern.getId()));
            member.setTeam(team);
            member.setUser(intern);
            member.setTeamRole("INTERN");
            teamMemberRepository.save(member);
            notifyTeamAllocated(team, intern, "INTERN", true);

            allocatedMembers.add(new TeamMemberResponse(
                    intern.getId(),
                    intern.getEmail(),
                    intern.getFirstName() + " " + intern.getLastName(),
                    "INTERN"
            ));
        }

        writeAudit(actor, "TEAM_AUTO_ALLOCATED", "TEAM", team.getId(),
                "Auto-allocated " + allocatedMembers.size() + " unallocated interns");

        return new AutoAllocateTeamMembersResponse(
                team.getId(),
                allocatedMembers.size(),
                MAX_TEAM_SIZE,
                allocatedMembers
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "team_members", key = "#teamId")
    public List<TeamMemberResponse> listTeamMembers(UUID teamId) {
        projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));

        return teamMemberRepository.findByTeam_IdOrderByCreatedAtAsc(teamId).stream()
                .map(this::toTeamMemberResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "cohort_teams", key = "#cohortId")
    public List<ProjectTeamResponse> listCohortTeams(UUID cohortId) {
        return projectTeamRepository.findByCohortId(cohortId).stream().map(this::toTeamResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "allocated_interns")
    public List<TeamMemberResponse> listAllAllocatedInterns() {
        return teamMemberRepository.findAllWithUser().stream()
                .map(this::toTeamMemberResponse)
                .toList();
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

    private TeamMemberResponse toTeamMemberResponse(TeamMember member) {
        return new TeamMemberResponse(
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getFirstName() + " " + member.getUser().getLastName(),
                member.getTeamRole()
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

    private void notifyTeamAllocated(ProjectTeam team, User user, String teamRole, boolean autoAllocated) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("teamId", team.getId());
        payload.put("teamName", team.getName());
        payload.put("cohortId", team.getCohort().getId());
        payload.put("teamRole", teamRole);
        payload.put("autoAllocated", autoAllocated);

        notificationService.notifyUser(
                user.getId(),
                "TEAM_ALLOCATED",
                "Team allocation update",
                "You have been allocated to team " + team.getName() + ".",
                payload
        );
    }
}
