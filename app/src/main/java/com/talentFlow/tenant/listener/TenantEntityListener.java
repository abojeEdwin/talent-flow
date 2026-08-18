package com.talentFlow.tenant.listener;

import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import com.talentFlow.tenant.TenantContextHolder;
import jakarta.persistence.PrePersist;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantEntityListener {

    @PrePersist
    public void onPrePersist(Object entity) {
        if (entity instanceof TenantAware) {
            UUID tenantId = TenantContextHolder.getTenantId();
            if (tenantId == null) {
                throw new IllegalStateException("Tenant ID not set in context");
            }
            Organization organization = new Organization();
            organization.setId(tenantId);
            ((TenantAware) entity).setOrganization(organization);
        }
    }
}
