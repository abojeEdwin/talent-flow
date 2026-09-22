package com.talentFlow.auth.infrastructure.repository;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import com.talentFlow.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Mongo replacement for the former JPA {@code NOT EXISTS (SELECT tm FROM TeamMember tm WHERE tm.user = u)}
 * queries. "Unallocated" users are simply users whose id appears in no team member document.
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private static final String TEAM_MEMBERS_COLLECTION = "team_members";

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<User> findUnallocatedInterns(RoleName role, Pageable pageable) {
        return run(role, null, null, pageable);
    }

    @Override
    public Page<User> findUnallocatedInternsByStatus(RoleName role, UserStatus status, Pageable pageable) {
        return run(role, status, null, pageable);
    }

    @Override
    public Page<User> searchUnallocatedInternsByQuery(RoleName role, String query, Pageable pageable) {
        return run(role, null, query, pageable);
    }

    @Override
    public Page<User> searchUnallocatedInternsByStatusAndQuery(RoleName role, UserStatus status, String query, Pageable pageable) {
        return run(role, status, query, pageable);
    }

    private Page<User> run(RoleName role, UserStatus status, String query, Pageable pageable) {
        Document filter = new Document("role", role.name());
        if (status != null) {
            filter.append("status", status.name());
        }
        if (query != null && !query.isBlank()) {
            filter.append("$or", searchConditions(query));
        }

        Set<UUID> allocatedUserIds = collectAllocatedUserIds();
        if (!allocatedUserIds.isEmpty()) {
            filter.append("_id", new Document("$nin", new ArrayList<>(allocatedUserIds)));
        }

        Query filterQuery = new BasicQuery(filter);
        List<User> content = mongoTemplate.find(filterQuery.with(pageable), User.class);
        long total = mongoTemplate.count(filterQuery, User.class);
        return new PageImpl<>(content, pageable, total);
    }

    private Set<UUID> collectAllocatedUserIds() {
        Query query = new Query();
        query.fields().include("user");
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            query.addCriteria(Criteria.where("organization.$id").is(tenantId));
        }
        List<Document> members = mongoTemplate.find(query, Document.class, TEAM_MEMBERS_COLLECTION);
        Set<UUID> ids = new HashSet<>();
        for (Document member : members) {
            Object reference = member.get("user");
            if (reference instanceof Document refDoc && refDoc.get("$id") instanceof UUID id) {
                ids.add(id);
            }
        }
        return ids;
    }

    private List<Document> searchConditions(String query) {
        Document regex = new Document("$regex", query).append("$options", "i");
        return List.of(
                new Document("email", regex),
                new Document("firstName", regex),
                new Document("lastName", regex)
        );
    }
}