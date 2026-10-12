package com.talentFlow.auth.service;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.dto.AuthResponse;
import com.talentFlow.data.dto.LoginRequest;
import com.talentFlow.data.dto.LoginResponse;
import com.talentFlow.data.dto.RegisterRequest;
import com.talentFlow.data.dto.RegisterResponse;
import com.talentFlow.data.dto.PasswordResetVerificationResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    AuthResponse currentUser(Authentication authentication);

    void logout();

    void resetPassword(String tokenValue, String newPassword);

    void requestPasswordReset(String email);

    PasswordResetVerificationResponse verifyPasswordResetPin(String email, String pin);

    String generatePasswordResetToken(User user);
}
