package com.talentFlow.auth.infrastructure.security;

import com.talentFlow.auth.domain.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailsService extends UserDetailsService {
    User loadDomainUserByEmail(String email);
}
