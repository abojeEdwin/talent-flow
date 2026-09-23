package com.talentFlow.admin.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "team_members")
public class TeamMember implements TenantAware {

    @Id
    private String id;

    @DBRef
    private Organization organization;

    @DBRef
    private ProjectTeam team;

    @DBRef
    private User user;

    private String teamRole;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static String buildId(UUID teamId, UUID userId) {
        return teamId + ":" + userId;
    }
}