package com.talentFlow.admin.web;

import com.talentFlow.admin.application.AdminProgramService;
import com.talentFlow.admin.web.dto.AllocateUserToTeamRequest;
import com.talentFlow.admin.web.dto.CohortResponse;
import com.talentFlow.admin.web.dto.CreateCohortRequest;
import com.talentFlow.admin.web.dto.CreateProjectTeamRequest;
import com.talentFlow.admin.web.dto.ProjectTeamResponse;
import com.talentFlow.admin.web.dto.TeamMemberResponse;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/programs")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PROGRAM_MANAGE')")
public class AdminProgramController {

    private final AdminProgramService adminProgramService;
    private final UserRepository userRepository;

    @PostMapping("/cohorts")
    public CohortResponse createCohort(@Valid @RequestBody CreateCohortRequest request, Authentication authentication) {
        return adminProgramService.createCohort(request, getActor(authentication));
    }

    @PostMapping("/teams")
    public ProjectTeamResponse createTeam(@Valid @RequestBody CreateProjectTeamRequest request, Authentication authentication) {
        return adminProgramService.createProjectTeam(request, getActor(authentication));
    }

    @PostMapping("/teams/{teamId}/members")
    public TeamMemberResponse allocateMember(
            @PathVariable UUID teamId,
            @Valid @RequestBody AllocateUserToTeamRequest request,
            Authentication authentication
    ) {
        return adminProgramService.allocateUserToTeam(teamId, request, getActor(authentication));
    }

    @GetMapping("/cohorts/{cohortId}/teams")
    public List<ProjectTeamResponse> listCohortTeams(@PathVariable UUID cohortId) {
        return adminProgramService.listCohortTeams(cohortId);
    }

    private User getActor(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not an authenticated user");
        }
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
