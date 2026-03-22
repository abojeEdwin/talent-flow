package com.talentFlow.auth.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.web.dto.AuthResponse;
import com.talentFlow.auth.web.dto.LoginRequest;
import com.talentFlow.auth.web.dto.RegisterRequest;
import com.talentFlow.auth.web.dto.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, HttpServletRequest httpServletRequest);

    void verifyEmail(String tokenValue);

    AuthResponse currentUser(Authentication authentication);

    void logout(HttpServletRequest request);

    void resetPassword(String tokenValue, String newPassword);

    String generatePasswordResetToken(User user);
}
