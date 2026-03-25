package com.app.eventnexus.config;

import com.app.eventnexus.tenant.TenantAwareDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Replaces the default Spring Boot {@link DataSource} with a
 * {@link TenantAwareDataSource} that sets the {@code app.tenant_id} PostgreSQL
 * session variable on every JDBC connection.
 *
 * <p>This is the mechanism that makes Row-Level Security work: each connection
 * handed to Hibernate already has the correct tenant ID set in the DB session,
 * so all subsequent queries on that connection are automatically filtered by
 * RLS policies.
 */
@Configuration
public class TenantDataSourceConfig {

    /**
     * Exposes the {@code spring.datasource.*} properties as a bean so we can
     * build the underlying data source programmatically.
     *
     * @return Spring Boot's data source properties
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Creates the primary {@link DataSource} wrapped with tenant-awareness.
     * The inner data source is built from {@code spring.datasource.*} properties
     * exactly as Spring Boot would build it automatically.
     *
     * @param properties the data source properties
     * @return a {@link TenantAwareDataSource} wrapping the standard HikariCP pool
     */
    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        DataSource inner = properties.initializeDataSourceBuilder().build();
        return new TenantAwareDataSource(inner);
    }
}
