package com.talentFlow.admin.domain;

import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Getter
@Setter
@Document(collection = "cohorts")
public class Cohort extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    private String name;

    private String description;

    private Integer intakeYear;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean isActive;
}