package com.talentFlow.admin.infrastructure.repository;

import com.talentFlow.admin.domain.TeamMember;
import com.talentFlow.admin.domain.TeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {
    List<TeamMember> findByTeam_Id(UUID teamId);
}
