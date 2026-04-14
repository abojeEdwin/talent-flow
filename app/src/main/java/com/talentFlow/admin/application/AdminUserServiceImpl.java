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
import com.talentFlow.notification.application.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final AuthMailService authMailService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

 //TODO:Cache responses from [List instructors, List users]

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
    public Page<AdminUserSummaryResponse> listInstructors(String query, UserStatus status, Pageable pageable) {
        Page<User> instructors;
        if (query != null && !query.isBlank()) {
            String trimmedQuery = query.trim();
            if (status != null) {
                instructors = userRepository.searchByRoleAndStatusAndQuery(
                        RoleName.INSTRUCTOR,
                        status,
                        trimmedQuery,
                        pageable
                );
            } else {
                instructors = userRepository.searchByRoleAndQuery(RoleName.INSTRUCTOR, trimmedQuery, pageable);
            }
        } else if (status != null) {
            instructors = userRepository.findByRoleAndStatus(RoleName.INSTRUCTOR, status, pageable);
        } else {
            instructors = userRepository.findByRole(RoleName.INSTRUCTOR, pageable);
        }
        return instructors.map(this::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserSummaryResponse> listUnallocatedInterns(String query, UserStatus status, Pageable pageable) {
        Page<User> interns;
        if (query != null && !query.isBlank()) {
            String trimmedQuery = query.trim();
            if (status != null) {
                interns = userRepository.searchUnallocatedInternsByStatusAndQuery(
                        RoleName.INTERN,
                        status,
                        trimmedQuery,
                        pageable
                );
            } else {
                interns = userRepository.searchUnallocatedInternsByQuery(RoleName.INTERN, trimmedQuery, pageable);
            }
        } else if (status != null) {
            interns = userRepository.findUnallocatedInternsByStatus(RoleName.INTERN, status, pageable);
        } else {
            interns = userRepository.findUnallocatedInterns(RoleName.INTERN, pageable);
        }
        return interns.map(this::toSummaryResponse);
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
        notifyUserStatusChanged(saved, previousStatus, newStatus);
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
        log.info(temporaryPassword);
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
        notifyUserDeactivated(saved, previousStatus);
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
        notifyPasswordResetTriggered(user);
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
        notifyRoleChanged(saved, previousRole, saved.getRole());
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

    private void notifyUserStatusChanged(User user, UserStatus previousStatus, UserStatus newStatus) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getId());
        payload.put("previousStatus", previousStatus.name());
        payload.put("newStatus", newStatus.name());

        notificationService.notifyUser(
                user.getId(),
                "ACCOUNT_STATUS_CHANGED",
                "Account status updated",
                "Your account status changed from " + previousStatus + " to " + newStatus + ".",
                payload
        );
    }

    private void notifyRoleChanged(User user, RoleName previousRole, RoleName newRole) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getId());
        payload.put("previousRole", previousRole.name());
        payload.put("newRole", newRole.name());

        notificationService.notifyUser(
                user.getId(),
                "ROLE_CHANGED",
                "Account role updated",
                "Your account role changed from " + previousRole + " to " + newRole + ".",
                payload
        );
    }

    private void notifyUserDeactivated(User user, UserStatus previousStatus) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getId());
        payload.put("previousStatus", previousStatus.name());
        payload.put("newStatus", UserStatus.DISABLED.name());

        notificationService.notifyUser(
                user.getId(),
                "ACCOUNT_DEACTIVATED",
                "Account deactivated",
                "Your account has been deactivated.",
                payload
        );
    }

    private void notifyPasswordResetTriggered(User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getId());
        payload.put("email", user.getEmail());

        notificationService.notifyUser(
                user.getId(),
                "PASSWORD_RESET_TRIGGERED",
                "Password reset initiated",
                "A password reset was initiated for your account. Check your email.",
                payload
        );
    }
}
