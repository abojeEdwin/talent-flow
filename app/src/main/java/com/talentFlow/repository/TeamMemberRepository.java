package com.talentFlow.repository;

import com.talentFlow.data.entity.TeamMember;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository extends MongoRepository<TeamMember, String>, TeamMemberRepositoryCustom {
    List<TeamMember> findByOrderByCreatedAtAsc();

    boolean existsByTeamIdAndUserId(String teamId, String userId);

    boolean existsByUserId(UUID userId);

    List<TeamMember> findByTeamIdOrderByCreatedAtAsc(UUID teamId);

    long countByTeamId(UUID teamId);
}