package com.talentFlow.common.service;

import com.talentFlow.data.entity.Organization;

public interface TenantAware {
    Organization getOrganization();
    void setOrganization(Organization organization);
}
