package com.talentFlow.admin.application;

import com.talentFlow.admin.web.dto.AdminUserDetailResponse;
import com.talentFlow.admin.web.dto.AdminUserSummaryResponse;
import com.talentFlow.admin.web.dto.CreateInstructorRequest;
import com.talentFlow.admin.web.dto.OnboardInstructorResponse;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {
    Page<AdminUserSummaryResponse> listUsers(String query, UserStatus status, Pageable pageable);

    Page<AdminUserSummaryResponse> listInstructors(String query, UserStatus status, Pageable pageable);

    Page<AdminUserSummaryResponse> listUnallocatedInterns(String query, UserStatus status, Pageable pageable);

    AdminUserDetailResponse getUser(UUID userId);

    AdminUserDetailResponse updateUserStatus(UUID userId, UserStatus newStatus, User actor);

    OnboardInstructorResponse onboardInstructor(CreateInstructorRequest request, User actor);

    AdminUserDetailResponse deactivateUser(UUID userId, User actor);

    void triggerPasswordReset(UUID userId, User actor);

    AdminUserDetailResponse updateUserRoles(UUID userId, RoleName role, User actor);
}
