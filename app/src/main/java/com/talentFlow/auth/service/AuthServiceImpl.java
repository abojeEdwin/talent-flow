package com.talentFlow.auth.service;

import com.talentFlow.auth.data.entity.PasswordResetToken;
import com.talentFlow.auth.data.entity.User;
import com.talentFlow.auth.data.enums.RoleName;
import com.talentFlow.auth.data.enums.UserStatus;
import com.talentFlow.repository.PasswordResetTokenRepository;
import com.talentFlow.repository.UserRepository;
import com.talentFlow.data.dto.AuthResponse;
import com.talentFlow.data.dto.LoginRequest;
import com.talentFlow.data.dto.LoginResponse;
import com.talentFlow.data.dto.RegisterRequest;
import com.talentFlow.data.dto.RegisterResponse;
import com.talentFlow.data.dto.PasswordResetVerificationResponse;
import com.talentFlow.common.security.JwtService;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.data.entity.Organization;
import com.talentFlow.repository.OrganizationRepository;
import com.talentFlow.mail.AuthMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMailService authMailService;

    @Value("${app.security.password-reset-expiry-hours:2}")
    private long passwordResetExpiryHours;

    @Value("${app.security.password-reset-max-pin-attempts:5}")
    private int passwordResetMaxPinAttempts;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Invalid signup credentials");
        }
        if (organizationRepository.existsByNameIgnoreCase(request.organizationName().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "An organization with this name already exists");
        }

        Organization organization = Organization.builder()
                .name(request.organizationName().trim())
                .description("Created for " + request.firstName().trim() + " " + request.lastName().trim())
                .build();
        Organization savedOrganization = organizationRepository.save(organization);

        User user = new User();
        user.setOrganization(savedOrganization);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(RoleName.ORG_ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        User savedUser = userRepository.save(user);
        String accessToken = jwtService.generateToken(savedUser.getEmail(), savedOrganization.getId());

        return new RegisterResponse(
                savedOrganization.getId(),
                savedUser.getId(),
                savedUser.getEmail(),
                accessToken,
                "Bearer",
                jwtService.getExpirationSeconds(),
                "Organization and admin account created successfully."
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.email().trim().toLowerCase(),
                            request.password()
                    )
            );

            User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            User savedUser = userRepository.save(user);
            String accessToken = jwtService.generateToken(
                    savedUser.getEmail(),
                    savedUser.getOrganization() == null ? null : savedUser.getOrganization().getId()
            );

            return new LoginResponse(
                    accessToken,
                    "Bearer",
                    jwtService.getExpirationSeconds(),
                    toAuthResponse(savedUser)
            );
        } catch (BadCredentialsException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        } catch (DisabledException exception) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is not active");
        } catch (LockedException exception) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is locked");
        }
    }

    @Override
    public AuthResponse currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not an authenticated user");
        }
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return toAuthResponse(user);
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid token"));

        if (token.getUsedAt() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reset token has already been used");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reset token has expired");
        }
        if (token.getPinHash() != null && token.getVerifiedAt() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reset PIN has not been verified");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        token.setUsedAt(LocalDateTime.now());
        userRepository.save(user);
        passwordResetTokenRepository.save(token);
    }

    @Override
    public void requestPasswordReset(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .ifPresent(user -> {
                    passwordResetTokenRepository.deleteByUser(user);

                    String pin = String.format("%06d", secureRandom.nextInt(1_000_000));
                    PasswordResetToken resetToken = new PasswordResetToken();
                    resetToken.setUser(user);
                    resetToken.setToken(UUID.randomUUID().toString());
                    resetToken.setPinHash(passwordEncoder.encode(pin));
                    resetToken.setPinAttempts(0);
                    resetToken.setExpiresAt(LocalDateTime.now().plusHours(passwordResetExpiryHours));
                    passwordResetTokenRepository.save(resetToken);

                    authMailService.sendPasswordResetPinEmail(user.getEmail(), user.getFirstName(), pin);
                });
    }

    @Override
    public PasswordResetVerificationResponse verifyPasswordResetPin(String email, String pin) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset PIN"));
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findFirstByUserAndUsedAtIsNullOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset PIN"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())
                || resetToken.getPinHash() == null
                || resetToken.getVerifiedAt() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset PIN");
        }

        int attempts = resetToken.getPinAttempts() == null ? 0 : resetToken.getPinAttempts();
        if (attempts >= passwordResetMaxPinAttempts) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Too many invalid reset PIN attempts");
        }
        if (!passwordEncoder.matches(pin, resetToken.getPinHash())) {
            resetToken.setPinAttempts(attempts + 1);
            passwordResetTokenRepository.save(resetToken);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset PIN");
        }

        resetToken.setVerifiedAt(LocalDateTime.now());
        resetToken.setToken(UUID.randomUUID().toString());
        passwordResetTokenRepository.save(resetToken);
        return new PasswordResetVerificationResponse(
                resetToken.getToken(),
                "PIN verified. You may now set a new password."
        );
    }

    @Override
    public String generatePasswordResetToken(User user) {
        passwordResetTokenRepository.deleteByUser(user);
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusHours(passwordResetExpiryHours));
        return passwordResetTokenRepository.save(token).getToken();
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                user.getId(),
                user.getOrganization() == null ? null : user.getOrganization().getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getStatus().name()
        );
    }
}