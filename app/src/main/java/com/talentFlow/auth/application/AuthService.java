package com.talentFlow.auth.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.web.dto.AuthResponse;
import com.talentFlow.auth.web.dto.LoginRequest;
import com.talentFlow.auth.web.dto.LoginResponse;
import com.talentFlow.auth.web.dto.RegisterRequest;
import com.talentFlow.auth.web.dto.RegisterResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    AuthResponse currentUser(Authentication authentication);

    void logout();

    void resetPassword(String tokenValue, String newPassword);

    String generatePasswordResetToken(User user);
}
