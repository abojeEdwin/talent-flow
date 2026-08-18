package com.talentFlow.tenant.config;

import com.talentFlow.tenant.TenantContextHolder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class HibernateFilterConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public void enableTenantFilter() {
        // In some cases, the security context might be initialized before the tenant context.
        // We only proceed if the entity manager is available.
        if (entityManager == null) {
            return;
        }

        Session session = entityManager.unwrap(Session.class);
        UUID tenantId = TenantContextHolder.getTenantId();

        // The filter is only enabled if a tenantId is present in the context.
        // This allows for operations that are not tenant-specific (e.g., system-level tasks).
        if (tenantId != null) {
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        }
    }
}
