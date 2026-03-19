package com.app.eventnexus.tenant;

/**
 * Thread-local holder for the current request's tenant (organization) ID.
 *
 * <p>{@code TenantFilter} sets the value early in the filter chain.
 * {@code TenantAwareDataSource} reads it before handing a JDBC connection to
 * Hibernate, issuing {@code SET LOCAL app.tenant_id = '<id>'} so PostgreSQL's
 * Row-Level Security policies can filter data to the correct tenant.
 *
 * <p>The value is always cleared in the {@code TenantFilter} finally block to
 * prevent ThreadLocal leaks across requests in a thread-pool environment.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
        // Utility class — no instances
    }

    /**
     * Stores the organization ID for the current request thread.
     *
     * @param tenantId the organization's primary key
     */
    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Returns the organization ID set for the current thread, or {@code null}
     * if no tenant context has been established (e.g. on public endpoints).
     *
     * @return the current tenant ID, or null
     */
    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Clears the tenant context for the current thread.
     * Must be called in a {@code finally} block to prevent leaks.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
