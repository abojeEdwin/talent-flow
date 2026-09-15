package com.talentFlow.tenant.listener;

import com.talentFlow.auth.infrastructure.security.UserPrincipal;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import com.talentFlow.tenant.TenantContextHolder;
import jakarta.persistence.PrePersist;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantEntityListener {

    @PrePersist
    public void onPrePersist(Object entity) {
        if (!(entity instanceof TenantAware aware)) {
            return;
        }

        Organization existing = aware.getOrganization();
        if (existing != null) {
            return;
        }

        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            tenantId = resolveFromPrincipal();
        }
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context available while persisting tenant-aware entity "
                    + entity.getClass().getSimpleName());
        }
        Organization organization = new Organization();
        organization.setId(tenantId);
        aware.setOrganization(organization);
    }

    private UUID resolveFromPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getOrganizationId();
        }
        return null;
    }
}