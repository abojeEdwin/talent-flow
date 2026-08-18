package com.talentFlow.tenant;

import java.util.UUID;

public final class TenantContextHolder {

    private static final ThreadLocal<UUID> CONTEXT = new ThreadLocal<>();

    private TenantContextHolder() {}

    public static void setTenantId(UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        CONTEXT.set(tenantId);
    }

    public static UUID getTenantId() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
