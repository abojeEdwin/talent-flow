package com.talentFlow.auth.infrastructure.repository;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    Page<User> findUnallocatedInterns(RoleName role, Pageable pageable);

    Page<User> findUnallocatedInternsByStatus(RoleName role, UserStatus status, Pageable pageable);

    Page<User> searchUnallocatedInternsByQuery(RoleName role, String query, Pageable pageable);

    Page<User> searchUnallocatedInternsByStatusAndQuery(RoleName role, UserStatus status, String query, Pageable pageable);
}