package com.talentFlow.auth.web;

import com.talentFlow.auth.application.AuthService;
import com.talentFlow.auth.web.dto.AuthResponse;
import com.talentFlow.auth.web.dto.LoginRequest;
import com.talentFlow.auth.web.dto.RegisterRequest;
import com.talentFlow.auth.web.dto.RegisterResponse;
import com.talentFlow.auth.web.dto.VerifyEmailRequest;
import com.talentFlow.common.response.ApiMessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(authService.login(request, httpServletRequest));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiMessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.token());
        return ResponseEntity.ok(new ApiMessageResponse("Email verified successfully"));
    }

    @GetMapping("/verify-email/confirm")
    public ResponseEntity<ApiMessageResponse> verifyEmailFromLink(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(new ApiMessageResponse("Email verified successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> currentUser(Authentication authentication) {
        return ResponseEntity.ok(authService.currentUser(authentication));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiMessageResponse> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new ApiMessageResponse("Logged out successfully"));
    }
}
