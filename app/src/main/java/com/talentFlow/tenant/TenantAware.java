package com.talentFlow.tenant;

import com.talentFlow.organization.domain.Organization;

public interface TenantAware {
    Organization getOrganization();
    void setOrganization(Organization organization);
}
