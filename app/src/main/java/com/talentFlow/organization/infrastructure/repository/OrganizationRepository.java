package com.talentFlow.organization.infrastructure.repository;

import com.talentFlow.organization.domain.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends MongoRepository<Organization, UUID> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Organization> findByNameIgnoreCase(String name);
}