package com.talentFlow.admin.application;

import com.talentFlow.admin.domain.AdminAuditLog;
import com.talentFlow.admin.infrastructure.repository.AdminAuditLogRepository;
import com.talentFlow.admin.web.dto.AdminUserDetailResponse;
import com.talentFlow.admin.web.dto.AdminUserSummaryResponse;
import com.talentFlow.admin.web.dto.CreateInstructorRequest;
import com.talentFlow.admin.web.dto.OnboardInstructorResponse;
import com.talentFlow.auth.application.AuthService;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import com.talentFlow.auth.infrastructure.mail.AuthMailService;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final AuthMailService authMailService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;



    //PASSWORD_RESET_FRONTEND_URL
    @Value("${app.security.password-reset-frontend-url}")
    private String passwordResetFrontendUrl;


    //LOGIN_URL
    @Value("${app.security.login-url}")
    private String loginUrl;

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUser(UUID userId) {
        User user = getUserOrThrow(userId);
        return toDetailResponse(user);
    }

    @Override
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

    @Override
    @Transactional
    public OnboardInstructorResponse onboardInstructor(CreateInstructorRequest request, User actor) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        String temporaryPassword = generateTemporaryPassword();
        User instructor = new User();
        instructor.setEmail(email);
        instructor.setFirstName(request.firstName().trim());
        instructor.setLastName(request.lastName().trim());
        instructor.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        instructor.setRole(RoleName.INSTRUCTOR);
        instructor.setStatus(UserStatus.ACTIVE);
        instructor.setFailedLoginAttempts(0);

        User saved = userRepository.save(instructor);
        authMailService.sendInstructorWelcomeEmail(
                saved.getEmail(),
                saved.getFirstName(),
                temporaryPassword,
                loginUrl
        );
        writeAudit(actor, "INSTRUCTOR_ONBOARDED", "USER", saved.getId(),
                "Onboarded instructor " + saved.getEmail());

        return new OnboardInstructorResponse(
                saved.getId(),
                saved.getEmail(),
                "Instructor onboarded successfully. Welcome email sent."
        );
    }

    @Override
    @Transactional
    public AdminUserDetailResponse deactivateUser(UUID userId, User actor) {
        User user = getUserOrThrow(userId);
        UserStatus previousStatus = user.getStatus();
        user.setStatus(UserStatus.DISABLED);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        User saved = userRepository.save(user);
        writeAudit(actor, "USER_DEACTIVATED", "USER", saved.getId(),
                "Soft-offboarded user. Previous status was " + previousStatus);
        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public void triggerPasswordReset(UUID userId, User actor) {
        User user = getUserOrThrow(userId);
        String resetToken = authService.generatePasswordResetToken(user);
        String resetLink = passwordResetFrontendUrl + "?token=" + resetToken;
        authMailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetLink);
        writeAudit(actor, "PASSWORD_RESET_TRIGGERED", "USER", user.getId(),
                "Admin initiated password reset");
    }

    @Override
    @Transactional
    public AdminUserDetailResponse updateUserRoles(UUID userId, RoleName role, User actor) {
        User user = getUserOrThrow(userId);
        RoleName previousRole = user.getRole();
        user.setRole(role);
        User saved = userRepository.save(user);

        writeAudit(actor, "USER_ROLES_UPDATED", "USER", saved.getId(),
                "Changed role from " + previousRole + " to " + saved.getRole());
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
                user.getRole().name(),
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
                user.getFailedLoginAttempts(),
                user.getLockedUntil(),
                user.getLastLoginAt(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }
}
