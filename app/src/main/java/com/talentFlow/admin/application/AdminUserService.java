package com.talentFlow.admin.application;

import com.talentFlow.admin.domain.AdminAuditLog;
import com.talentFlow.admin.infrastructure.repository.AdminAuditLogRepository;
import com.talentFlow.admin.web.dto.AdminUserDetailResponse;
import com.talentFlow.admin.web.dto.AdminUserSummaryResponse;
import com.talentFlow.auth.domain.Role;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import com.talentFlow.auth.infrastructure.repository.RoleRepository;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserSummaryResponse> listUsers(String query, UserStatus status, Pageable pageable) {
        Page<User> users;
        if (query != null && !query.isBlank()) {
            users = userRepository.searchByQuery(query.trim(), pageable);
        } else if (status != null) {
            users = userRepository.findByStatus(status, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(this::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUser(UUID userId) {
        User user = getUserOrThrow(userId);
        return toDetailResponse(user);
    }

    @Transactional
    public AdminUserDetailResponse updateUserStatus(UUID userId, UserStatus newStatus, User actor) {
        User user = getUserOrThrow(userId);
        UserStatus previousStatus = user.getStatus();

        user.setStatus(newStatus);
        if (newStatus == UserStatus.ACTIVE) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
        }

        User saved = userRepository.save(user);
        writeAudit(actor, "USER_STATUS_UPDATED", "USER", saved.getId(),
                "Changed status from " + previousStatus + " to " + newStatus);
        return toDetailResponse(saved);
    }

    @Transactional
    public AdminUserDetailResponse updateUserRoles(UUID userId, Set<RoleName> roleNames, User actor) {
        User user = getUserOrThrow(userId);
        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Role " + roleName + " does not exist")))
                .collect(Collectors.toSet());

        Set<String> previousRoles = user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet());
        user.setRoles(roles);
        User saved = userRepository.save(user);

        Set<String> newRoles = saved.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet());
        writeAudit(actor, "USER_ROLES_UPDATED", "USER", saved.getId(),
                "Changed roles from " + previousRoles + " to " + newRoles);
        return toDetailResponse(saved);
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
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

    private AdminUserSummaryResponse toSummaryResponse(User user) {
        return new AdminUserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus().name(),
                user.isEmailVerified(),
                mapRoleNames(user),
                user.getLastLoginAt()
        );
    }

    private AdminUserDetailResponse toDetailResponse(User user) {
        return new AdminUserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus().name(),
                user.isEmailVerified(),
                user.getFailedLoginAttempts(),
                user.getLockedUntil(),
                user.getLastLoginAt(),
                mapRoleNames(user),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private Set<String> mapRoleNames(User user) {
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
