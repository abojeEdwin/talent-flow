package com.talentFlow.auth.application;

import com.talentFlow.auth.domain.PasswordResetToken;
import com.talentFlow.auth.domain.Role;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.VerificationToken;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import com.talentFlow.auth.infrastructure.mail.AuthMailService;
import com.talentFlow.auth.infrastructure.repository.PasswordResetTokenRepository;
import com.talentFlow.auth.infrastructure.repository.RoleRepository;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.auth.infrastructure.repository.VerificationTokenRepository;
import com.talentFlow.auth.web.dto.AuthResponse;
import com.talentFlow.auth.web.dto.LoginRequest;
import com.talentFlow.auth.web.dto.RegisterRequest;
import com.talentFlow.auth.web.dto.RegisterResponse;
import com.talentFlow.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthMailService authMailService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.verification-token-expiry-hours:24}")
    private long verificationTokenExpiryHours;

    @Value("${app.security.verification-token-frontend-url}")
    private String verificationTokenFrontendUrl;

    @Value("${app.security.password-reset-expiry-hours:2}")
    private long passwordResetExpiryHours;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        Role internRole = roleRepository.findByName(RoleName.INTERN)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Default INTERN role is missing"));

        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.setFailedLoginAttempts(0);
        user.getRoles().add(internRole);

        User savedUser = userRepository.save(user);

        VerificationToken token = new VerificationToken();
        token.setUser(savedUser);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusHours(verificationTokenExpiryHours));
        verificationTokenRepository.save(token);

        String verificationLink = verificationTokenFrontendUrl + "?token=" + token.getToken();
        authMailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFirstName(), verificationLink);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                "User registered successfully. Verify the email before login."
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.email().trim().toLowerCase(),
                            request.password()
                    )
            );

            User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            httpServletRequest.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context
            );

            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);

            return toAuthResponse(userRepository.save(user));
        } catch (BadCredentialsException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        } catch (DisabledException exception) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is not active");
        } catch (LockedException exception) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is locked");
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String tokenValue) {
        VerificationToken token = verificationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid verification token"));

        if (token.getUsedAt() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Verification token has already been used");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Verification token has expired");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        token.setUsedAt(LocalDateTime.now());

        userRepository.save(user);
        verificationTokenRepository.save(token);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not an authenticated user");
        }
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return toAuthResponse(user);
    }

    @Override
    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
    }

    @Override
    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid password reset token"));

        if (token.getUsedAt() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password reset token has already been used");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password reset token has expired");
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
    @Transactional
    public String generatePasswordResetToken(User user) {
        passwordResetTokenRepository.deleteByUser(user);
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusHours(passwordResetExpiryHours));
        return passwordResetTokenRepository.save(token).getToken();
    }

    private AuthResponse toAuthResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roles,
                user.isEmailVerified(),
                user.getStatus().name()
        );
    }
}
