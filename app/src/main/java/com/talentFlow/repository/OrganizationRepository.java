package com.talentFlow.repository;

import com.talentFlow.data.entity.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends MongoRepository<Organization, UUID> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Organization> findByNameIgnoreCase(String name);
}