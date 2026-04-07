package com.talentFlow.auth.web;

import com.talentFlow.auth.application.AuthService;
import com.talentFlow.auth.web.dto.AuthResponse;
import com.talentFlow.auth.web.dto.LoginRequest;
import com.talentFlow.auth.web.dto.LoginResponse;
import com.talentFlow.auth.web.dto.RegisterRequest;
import com.talentFlow.auth.web.dto.RegisterResponse;
import com.talentFlow.auth.web.dto.ResetPasswordRequest;
import com.talentFlow.common.response.ApiMessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> currentUser(Authentication authentication) {
        return ResponseEntity.ok(authService.currentUser(authentication));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiMessageResponse> logout() {
        authService.logout();
        return ResponseEntity.ok(new ApiMessageResponse("Logged out successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiMessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new ApiMessageResponse("Password reset successfully"));
    }
}
