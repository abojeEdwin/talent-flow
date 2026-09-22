package com.talentFlow.admin.infrastructure.repository;

import com.talentFlow.admin.domain.TeamMember;
import com.talentFlow.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TeamMemberRepositoryImpl implements TeamMemberRepositoryCustom {

    private static final String PROJECT_TEAMS_COLLECTION = "project_teams";
    private static final String TEAM_MEMBERS_COLLECTION = "team_members";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<TeamMember> findByTeamCohortId(UUID cohortId) {
        Query teamQuery = new Query();
        teamQuery.addCriteria(Criteria.where("cohort.$id").is(cohortId));
        teamQuery.fields().include("_id");
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            teamQuery.addCriteria(Criteria.where("organization.$id").is(tenantId));
        }
        List<Document> teamDocs = mongoTemplate.find(teamQuery, Document.class, PROJECT_TEAMS_COLLECTION);
        if (teamDocs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object> teamIds = teamDocs.stream().map(doc -> doc.get("_id")).toList();

        Query memberQuery = new Query();
        memberQuery.addCriteria(Criteria.where("team.$id").in(teamIds));
        if (tenantId != null) {
            memberQuery.addCriteria(Criteria.where("organization.$id").is(tenantId));
        }
        return mongoTemplate.find(memberQuery, TeamMember.class);
    }
}