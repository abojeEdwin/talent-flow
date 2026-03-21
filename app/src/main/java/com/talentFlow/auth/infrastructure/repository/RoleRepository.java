package com.talentFlow.auth.infrastructure.repository;

import com.talentFlow.auth.domain.Role;
import com.talentFlow.auth.domain.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleName name);
}
