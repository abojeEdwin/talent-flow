package com.talentFlow.admin.domain;

import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "project_teams")
public class ProjectTeam extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private Cohort cohort;

    private String name;

    private String description;
}