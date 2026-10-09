package com.talentFlow.repository;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.auth.data.enums.RoleName;
import com.talentFlow.auth.data.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    Page<User> findUnallocatedInterns(RoleName role, Pageable pageable);

    Page<User> findUnallocatedInternsByStatus(RoleName role, UserStatus status, Pageable pageable);

    Page<User> searchUnallocatedInternsByQuery(RoleName role, String query, Pageable pageable);

    Page<User> searchUnallocatedInternsByStatusAndQuery(RoleName role, UserStatus status, String query, Pageable pageable);
}