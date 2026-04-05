package com.talentFlow.auth.infrastructure.security;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.enabled:${ADMIN_SEED_ENABLED:false}}")
    private boolean enabled;

    @Value("${app.seed.admin.email:${ADMIN_SEED_EMAIL:}}")
    private String adminEmail;

    @Value("${app.seed.admin.password:${ADMIN_SEED_PASSWORD:}}")
    private String adminPassword;

    @Value("${app.seed.admin.first-name:${ADMIN_SEED_FIRST_NAME:System}}")
    private String firstName;

    @Value("${app.seed.admin.last-name:${ADMIN_SEED_LAST_NAME:Admin}}")
    private String lastName;

    @Value("${app.seed.admin.update-password:${ADMIN_SEED_UPDATE_PASSWORD:false}}")
    private boolean updatePassword;

    @Override
    public void run(String... args) {
        try {
            String normalizedEmail = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
            log.info("Admin seeder startup. enabled={}, emailConfigured={}", enabled, !normalizedEmail.isBlank());

            if (!enabled) {
                log.info("Admin seeder is disabled (app.seed.admin.enabled=false)");
                return;
            }

            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                throw new IllegalStateException("Admin seeding is enabled, but app.seed.admin.email/password are missing");
            }

            User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                    .orElseGet(() -> {
                        User created = new User();
                        created.setEmail(normalizedEmail);
                        created.setFirstName(firstName.trim());
                        created.setLastName(lastName.trim());
                        created.setPasswordHash(passwordEncoder.encode(adminPassword));
                        created.setRole(RoleName.ADMIN);
                        created.setStatus(UserStatus.ACTIVE);
                        created.setEmailVerified(true);
                        created.setFailedLoginAttempts(0);
                        created.setLockedUntil(null);
                        return created;
                    });

            boolean changed = false;
            if (user.getFirstName() == null || user.getFirstName().isBlank()) {
                user.setFirstName(firstName.trim());
                changed = true;
            }
            if (user.getLastName() == null || user.getLastName().isBlank()) {
                user.setLastName(lastName.trim());
                changed = true;
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                user.setStatus(UserStatus.ACTIVE);
                changed = true;
            }
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                changed = true;
            }
            if (updatePassword) {
                user.setPasswordHash(passwordEncoder.encode(adminPassword));
                changed = true;
            }
            if (user.getRole() != RoleName.ADMIN) {
                user.setRole(RoleName.ADMIN);
                changed = true;
            }
            if (user.getFailedLoginAttempts() != 0) {
                user.setFailedLoginAttempts(0);
                changed = true;
            }
            if (user.getLockedUntil() != null) {
                user.setLockedUntil(null);
                changed = true;
            }

            if (user.getId() == null || changed) {
                userRepository.saveAndFlush(user);
                log.info("Admin user seeded/updated successfully: {}", normalizedEmail);
            } else {
                log.info("Admin user already exists and is ready: {}", normalizedEmail);
            }
        } catch (Exception ex) {
            log.error("Failed to seed admin user", ex);
        }
    }
}
