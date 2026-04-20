package com.talentFlow.auth.application;

import com.talentFlow.auth.domain.PasswordResetToken;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import com.talentFlow.auth.infrastructure.repository.PasswordResetTokenRepository;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.auth.web.dto.AuthResponse;
import com.talentFlow.auth.web.dto.LoginRequest;
import com.talentFlow.auth.web.dto.LoginResponse;
import com.talentFlow.auth.web.dto.RegisterRequest;
import com.talentFlow.auth.web.dto.RegisterResponse;
import com.talentFlow.auth.infrastructure.security.JwtService;
import com.talentFlow.common.exception.ApiException;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.security.password-reset-expiry-hours:2}")
    private long passwordResetExpiryHours;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Invalid signup credentials");
        }

        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(RoleName.INTERN);
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                "User registered successfully."
        );
    }

    @Override
    @Transactional
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
            String accessToken = jwtService.generateToken(savedUser.getEmail());

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
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid token"));

        if (token.getUsedAt() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reset token has already been used");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reset token has expired");
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
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getStatus().name()
        );
    }
}
