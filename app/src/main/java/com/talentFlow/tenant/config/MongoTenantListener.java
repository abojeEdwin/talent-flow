package com.talentFlow.tenant.config;

import com.talentFlow.admin.domain.TeamMember;
import com.talentFlow.auth.infrastructure.security.UserPrincipal;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import com.talentFlow.tenant.TenantContextHolder;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Replaces the former JPA {@code TenantEntityListener} and {@code @PrePersist}
 * {@code BaseEntity} callbacks. Runs for every MongoDB document conversion and
 * assigns ids, timestamps and the tenant organization before the document is
 * persisted.
 */
@Component
public class MongoTenantListener extends AbstractMongoEventListener<Object> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object source = event.getSource();
        LocalDateTime now = LocalDateTime.now();

        if (source instanceof BaseEntity base) {
            if (base.getId() == null) {
                base.setId(UUID.randomUUID());
            }
            if (base.getCreatedAt() == null) {
                base.setCreatedAt(now);
            }
            base.setUpdatedAt(now);
        } else if (source instanceof TeamMember teamMember) {
            if (teamMember.getCreatedAt() == null) {
                teamMember.setCreatedAt(now);
            }
            teamMember.setUpdatedAt(now);
        }

        if (source instanceof TenantAware aware && aware.getOrganization() == null) {
            UUID tenantId = TenantContextHolder.getTenantId();
            if (tenantId == null) {
                tenantId = resolveFromPrincipal();
            }
            if (tenantId == null) {
                throw new IllegalStateException("No tenant context available while persisting tenant-aware entity "
                        + source.getClass().getSimpleName());
            }
            Organization organization = new Organization();
            organization.setId(tenantId);
            aware.setOrganization(organization);
        }
    }

    private UUID resolveFromPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getOrganizationId();
        }
        return null;
    }
}