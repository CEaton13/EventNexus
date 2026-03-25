package com.app.eventnexus.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A {@link DataSource} wrapper that sets the PostgreSQL session variable
 * {@code app.tenant_id} on every connection before returning it to the caller.
 *
 * <p>Row-Level Security policies on {@code tournaments}, {@code venues},
 * {@code equipment}, {@code matches}, and {@code equipment_loadouts} all read
 * this session variable via {@code current_setting('app.tenant_id', true)}.
 * Without it being set, those tables return zero rows for all tenants.
 *
 * <p>Registered as the primary {@code DataSource} bean in
 * {@link com.app.eventnexus.config.TenantDataSourceConfig}.
 */
public class TenantAwareDataSource extends DelegatingDataSource {

    private static final Logger log = LoggerFactory.getLogger(TenantAwareDataSource.class);

    public TenantAwareDataSource(DataSource targetDataSource) {
        super(targetDataSource);
    }

    /**
     * Returns a JDBC connection with {@code app.tenant_id} set to the current
     * thread's tenant ID (from {@link TenantContext}).
     * If no tenant is active (e.g. public endpoints), the variable is cleared
     * to avoid leaking a previous value from a pooled connection.
     *
     * @return the connection with the tenant session variable set
     * @throws SQLException if the underlying data source throws
     */
    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        applyTenantId(connection);
        return connection;
    }

    /**
     * Returns a JDBC connection (with credentials) with {@code app.tenant_id}
     * set to the current thread's tenant ID.
     *
     * @param username the DB username
     * @param password the DB password
     * @return the connection with the tenant session variable set
     * @throws SQLException if the underlying data source throws
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        applyTenantId(connection);
        return connection;
    }

    /**
     * Issues {@code SET SESSION app.tenant_id} on the given connection.
     * {@code SET SESSION} (the default scope for SET) persists for the lifetime
     * of the connection, which is safe here because {@code getConnection()} is
     * called on every HikariCP pool borrow — the value is always overwritten
     * before any SQL runs. {@code SET LOCAL} was previously used but is a no-op
     * when the connection is still in autocommit mode (Spring/Hibernate calls
     * {@code setAutoCommit(false)} *after* borrowing the connection), causing
     * RLS WITH CHECK violations during seeding and other non-transactional paths.
     *
     * @param connection the JDBC connection to configure
     * @throws SQLException if the SET statement fails
     */
    private void applyTenantId(Connection connection) throws SQLException {
        Long tenantId = TenantContext.getTenantId();
        try (Statement stmt = connection.createStatement()) {
            if (tenantId != null) {
                stmt.execute("SET SESSION app.tenant_id = '" + tenantId + "'");
                log.debug("Set app.tenant_id = {} on connection", tenantId);
            } else {
                // Clear any residual value from a pooled connection
                stmt.execute("SET SESSION app.tenant_id = ''");
            }
        }
    }
}
