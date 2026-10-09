package com.talentFlow.common.security;

import com.talentFlow.auth.data.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailsService extends UserDetailsService {
    User loadDomainUserByEmail(String email);
}
