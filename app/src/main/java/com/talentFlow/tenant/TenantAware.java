package com.talentFlow.tenant;

import com.talentFlow.organization.domain.Organization;

public interface TenantAware {
    void setOrganization(Organization organization);
}
