package io.dropwizard.db;

import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ManagedPooledDataSource extends HikariDataSource implements ManagedDataSource {
    /**
     * Create a new data source with the given connection pool configuration.
     *
     * @param config
     *            the connection pool configuration
     * @param metricRegistry
     *            the metric registry used to register the connection pool
     *            metrics.
     */
    public ManagedPooledDataSource(final HikariConfig config, final MetricRegistry metricRegistry) {
        super(config);
        this.setMetricRegistry(metricRegistry);
    }

    // JDK6 has JDBC 4.0 which doesn't have this -- don't add @Override
    @Override
    @SuppressWarnings("override")
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Doesn't use java.util.logging");
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
        this.close();
    }
}