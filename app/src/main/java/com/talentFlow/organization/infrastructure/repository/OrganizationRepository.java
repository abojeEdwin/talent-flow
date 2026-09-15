package com.talentFlow.organization.infrastructure.repository;

import com.talentFlow.organization.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Organization> findByNameIgnoreCase(String name);
}