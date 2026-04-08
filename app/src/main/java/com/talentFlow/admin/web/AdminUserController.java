package com.talentFlow.admin.web;

import com.talentFlow.admin.application.AdminUserService;
import com.talentFlow.admin.web.dto.AdminUserDetailResponse;
import com.talentFlow.admin.web.dto.AdminUserSummaryResponse;
import com.talentFlow.admin.web.dto.CreateInstructorRequest;
import com.talentFlow.admin.web.dto.OnboardInstructorResponse;
import com.talentFlow.admin.web.dto.UpdateUserRolesRequest;
import com.talentFlow.admin.web.dto.UpdateUserStatusRequest;
import com.talentFlow.common.response.ApiMessageResponse;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.UserStatus;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final UserRepository userRepository;

    @GetMapping("/")
    public Page<AdminUserSummaryResponse> listUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable
    ) {
        return adminUserService.listUsers(query, status, pageable);
    }

    @GetMapping("/instructors")
    public Page<AdminUserSummaryResponse> listInstructors(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable
    ) {
        return adminUserService.listInstructors(query, status, pageable);
    }

    @GetMapping("/interns/unallocated")
    public Page<AdminUserSummaryResponse> listUnallocatedInterns(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable
    ) {
        return adminUserService.listUnallocatedInterns(query, status, pageable);
    }

    @GetMapping("/{userId}")
    public AdminUserDetailResponse getUser(@PathVariable UUID userId) {
        return adminUserService.getUser(userId);
    }

    @PatchMapping("/{userId}/status")
    public AdminUserDetailResponse updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request,
            Authentication authentication
    ) {
        return adminUserService.updateUserStatus(userId, request.status(), getActor(authentication));
    }

    @PatchMapping("/{userId}/roles")
    public AdminUserDetailResponse updateUserRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRolesRequest request,
            Authentication authentication
    ) {
        return adminUserService.updateUserRoles(userId, request.role(), getActor(authentication));
    }

    @PostMapping("/instructors")
    public OnboardInstructorResponse onboardInstructor(
            @Valid @RequestBody CreateInstructorRequest request,
            Authentication authentication
    ) {
        return adminUserService.onboardInstructor(request, getActor(authentication));
    }

    @PatchMapping("/{userId}/deactivate")
    public AdminUserDetailResponse deactivateUser(@PathVariable UUID userId, Authentication authentication) {
        return adminUserService.deactivateUser(userId, getActor(authentication));
    }

    @PostMapping("/{userId}/password-reset")
    public ApiMessageResponse triggerPasswordReset(@PathVariable UUID userId, Authentication authentication) {
        adminUserService.triggerPasswordReset(userId, getActor(authentication));
        return new ApiMessageResponse("Password reset email sent");
    }

    private User getActor(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not an authenticated user");
        }
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
