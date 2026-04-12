package com.talentFlow.admin.infrastructure.repository;

import com.talentFlow.admin.domain.TeamMember;
import com.talentFlow.admin.domain.TeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {
    List<TeamMember> findByTeam_Id(UUID teamId);

    List<TeamMember> findByTeam_IdOrderByCreatedAtAsc(UUID teamId);

    List<TeamMember> findByTeam_Cohort_Id(UUID cohortId);

    long countByTeam_Id(UUID teamId);

    @Query("SELECT tm FROM TeamMember tm WHERE LOWER(tm.teamRole) = 'intern' ORDER BY tm.createdAt DESC")
    List<TeamMember> findAllInterns();

    boolean existsByUser_Id(UUID userId);
}
