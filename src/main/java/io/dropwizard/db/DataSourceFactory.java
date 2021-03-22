package io.dropwizard.db;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;

import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import io.dropwizard.validation.ValidationMethod;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSourceFactory implements PooledDataSourceFactory {
    public enum TransactionIsolation {
        TRANSACTION_NONE, TRANSACTION_READ_UNCOMMITTED, TRANSACTION_READ_COMMITTED, TRANSACTION_REPEATABLE_READ, TRANSACTION_SERIALIZABLE;
    }

    @NotNull
    private String driverClass = null;

    private boolean alternateUsernamesAllowed = false;

    private boolean commitOnReturn = false;

    private Optional<Boolean> autoCommitByDefault = Optional.empty();

    private Optional<Boolean> readOnlyByDefault = Optional.empty();

    private String user = null;

    private String password = null;

    @NotNull
    private String url = null;

    @NotNull
    private Map<String, String> properties = Maps.newLinkedHashMap();

    private String defaultCatalog;

    private Optional<TransactionIsolation> defaultTransactionIsolation = Optional.empty();

    private boolean useFairQueue = true;

    @Min(1)
    private int minSize = 10;

    @Min(1)
    private int maxSize = 100;

    private String initializationQuery;

    private boolean logAbandonedConnections = false;

    private boolean logValidationErrors = false;

    @MinDuration(value = 1, unit = TimeUnit.SECONDS)
    private Duration maxConnectionAge;

    @NotNull
    @MinDuration(value = 1, unit = TimeUnit.SECONDS)
    private Duration maxWaitForConnection = Duration.seconds(30);

    @NotNull
    @MinDuration(value = 1, unit = TimeUnit.SECONDS)
    private Duration minIdleTime = Duration.minutes(1);

    @NotNull
    private String validationQuery = "/* Health Check */ SELECT 1";

    @MinDuration(value = 1, unit = TimeUnit.SECONDS)
    private Duration validationQueryTimeout;

    @MinDuration(value = 1, unit = TimeUnit.SECONDS)
    private Duration connectionTimeout;

    @MinDuration(value = 1, unit = TimeUnit.SECONDS)
    private Duration idleTimeout;

    @MinDuration(value = 1, unit = TimeUnit.SECONDS)
    private Duration initializationFailTimeout;

    @MinDuration(value = 1, unit = TimeUnit.SECONDS)
    private Duration validationTimeout;

    private boolean checkConnectionWhileIdle = true;

    private boolean checkConnectionOnBorrow = false;

    private boolean checkConnectionOnConnect = true;

    private boolean checkConnectionOnReturn = false;

    private boolean autoCommentsEnabled = true;

    @NotNull
    @MinDuration(1)
    private Duration evictionInterval = Duration.seconds(5);

    @NotNull
    @MinDuration(1)
    private Duration validationInterval = Duration.seconds(30);

    private Optional<String> validatorClassName = Optional.empty();

    private boolean removeAbandoned = false;

    @NotNull
    @MinDuration(1)
    private Duration removeAbandonedTimeout = Duration.seconds(60L);

    @JsonProperty
    public Optional<Duration> getConnectionTimeout() {
        return Optional.ofNullable(this.connectionTimeout);
    }

    public void setConnectionTimeout(final Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @JsonProperty
    public Optional<Duration> getIdleTimeout() {
        return Optional.ofNullable(this.idleTimeout);
    }

    public void setIdleTimeout(final Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @JsonProperty
    public Optional<Duration> getInitializationFailTimeout() {
        return Optional.ofNullable(this.initializationFailTimeout);
    }

    public void setInitializationFailTimeout(final Duration initializationFailTimeout) {
        this.initializationFailTimeout = initializationFailTimeout;
    }

    @JsonProperty
    public Optional<Duration> getValidationTimeout() {
        return Optional.ofNullable(this.validationTimeout);
    }

    public void setValidationTimeout(final Duration validationTimeout) {
        this.validationTimeout = validationTimeout;
    }

    @JsonProperty
    @Override
    public boolean isAutoCommentsEnabled() {
        return this.autoCommentsEnabled;
    }

    @JsonProperty
    public void setAutoCommentsEnabled(final boolean autoCommentsEnabled) {
        this.autoCommentsEnabled = autoCommentsEnabled;
    }

    @JsonProperty
    @Override
    public String getDriverClass() {
        return this.driverClass;
    }

    @JsonProperty
    public void setDriverClass(final String driverClass) {
        this.driverClass = driverClass;
    }

    @JsonProperty
    public String getUser() {
        return this.user;
    }

    @JsonProperty
    public void setUser(final String user) {
        this.user = user;
    }

    @JsonProperty
    public String getPassword() {
        return this.password;
    }

    @JsonProperty
    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    @JsonProperty
    public String getUrl() {
        return this.url;
    }

    @JsonProperty
    public void setUrl(final String url) {
        this.url = url;
    }

    @JsonProperty
    @Override
    public Map<String, String> getProperties() {
        return this.properties;
    }

    @JsonProperty
    public void setProperties(final Map<String, String> properties) {
        this.properties = properties;
    }

    @JsonProperty
    public Duration getMaxWaitForConnection() {
        return this.maxWaitForConnection;
    }

    @JsonProperty
    public void setMaxWaitForConnection(final Duration maxWaitForConnection) {
        this.maxWaitForConnection = maxWaitForConnection;
    }

    @Override
    @JsonProperty
    public String getValidationQuery() {
        return this.validationQuery;
    }

    @Override
    @Deprecated
    @JsonIgnore
    public String getHealthCheckValidationQuery() {
        return this.getValidationQuery();
    }

    @JsonProperty
    public void setValidationQuery(final String validationQuery) {
        this.validationQuery = validationQuery;
    }

    @JsonProperty
    public int getMinSize() {
        return this.minSize;
    }

    @JsonProperty
    public void setMinSize(final int minSize) {
        this.minSize = minSize;
    }

    @JsonProperty
    public int getMaxSize() {
        return this.maxSize;
    }

    @JsonProperty
    public void setMaxSize(final int maxSize) {
        this.maxSize = maxSize;
    }

    @JsonProperty
    public boolean getCheckConnectionWhileIdle() {
        return this.checkConnectionWhileIdle;
    }

    @JsonProperty
    public void setCheckConnectionWhileIdle(final boolean checkConnectionWhileIdle) {
        this.checkConnectionWhileIdle = checkConnectionWhileIdle;
    }

    @Deprecated
    @JsonProperty
    public boolean isDefaultReadOnly() {
        return Boolean.TRUE.equals(this.readOnlyByDefault);
    }

    @Deprecated
    @JsonProperty
    public void setDefaultReadOnly(final boolean defaultReadOnly) {
        this.readOnlyByDefault = Optional.of(defaultReadOnly);
    }

    @JsonIgnore
    @ValidationMethod(message = ".minSize must be less than or equal to maxSize")
    public boolean isMinSizeLessThanMaxSize() {
        return this.minSize <= this.maxSize;
    }

    @JsonProperty
    public boolean isAlternateUsernamesAllowed() {
        return this.alternateUsernamesAllowed;
    }

    @JsonProperty
    public void setAlternateUsernamesAllowed(final boolean allow) {
        this.alternateUsernamesAllowed = allow;
    }

    @JsonProperty
    public boolean getCommitOnReturn() {
        return this.commitOnReturn;
    }

    @JsonProperty
    public void setCommitOnReturn(final boolean commitOnReturn) {
        this.commitOnReturn = commitOnReturn;
    }

    @JsonProperty
    @Nullable
    public Boolean getAutoCommitByDefault() {
        return this.autoCommitByDefault.orElse(null);
    }

    @JsonProperty
    public void setAutoCommitByDefault(final Boolean autoCommit) {
        this.autoCommitByDefault = Optional.ofNullable(autoCommit);
    }

    @JsonProperty
    public String getDefaultCatalog() {
        return this.defaultCatalog;
    }

    @JsonProperty
    public void setDefaultCatalog(final String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    @JsonProperty
    public Optional<Boolean> getReadOnlyByDefault() {
        return this.readOnlyByDefault;
    }

    @JsonProperty
    public void setReadOnlyByDefault(final Optional<Boolean> readOnlyByDefault) {
        this.readOnlyByDefault = readOnlyByDefault;
    }

    @JsonProperty
    public Optional<TransactionIsolation> getDefaultTransactionIsolation() {
        return this.defaultTransactionIsolation;
    }

    @JsonProperty
    public void setDefaultTransactionIsolation(final Optional<TransactionIsolation> isolation) {
        this.defaultTransactionIsolation = isolation;
    }

    @JsonProperty
    public boolean getUseFairQueue() {
        return this.useFairQueue;
    }

    @JsonProperty
    public void setUseFairQueue(final boolean fair) {
        this.useFairQueue = fair;
    }

    @JsonProperty
    public String getInitializationQuery() {
        return this.initializationQuery;
    }

    @JsonProperty
    public void setInitializationQuery(final String query) {
        this.initializationQuery = query;
    }

    @JsonProperty
    public boolean getLogAbandonedConnections() {
        return this.logAbandonedConnections;
    }

    @JsonProperty
    public void setLogAbandonedConnections(final boolean log) {
        this.logAbandonedConnections = log;
    }

    @JsonProperty
    public boolean getLogValidationErrors() {
        return this.logValidationErrors;
    }

    @JsonProperty
    public void setLogValidationErrors(final boolean log) {
        this.logValidationErrors = log;
    }

    @JsonProperty
    public Optional<Duration> getMaxConnectionAge() {
        return Optional.ofNullable(this.maxConnectionAge);
    }

    @JsonProperty
    public void setMaxConnectionAge(final Duration age) {
        this.maxConnectionAge = age;
    }

    @JsonProperty
    public Duration getMinIdleTime() {
        return this.minIdleTime;
    }

    @JsonProperty
    public void setMinIdleTime(final Duration time) {
        this.minIdleTime = time;
    }

    @JsonProperty
    public boolean getCheckConnectionOnBorrow() {
        return this.checkConnectionOnBorrow;
    }

    @JsonProperty
    public void setCheckConnectionOnBorrow(final boolean checkConnectionOnBorrow) {
        this.checkConnectionOnBorrow = checkConnectionOnBorrow;
    }

    @JsonProperty
    public boolean getCheckConnectionOnConnect() {
        return this.checkConnectionOnConnect;
    }

    @JsonProperty
    public void setCheckConnectionOnConnect(final boolean checkConnectionOnConnect) {
        this.checkConnectionOnConnect = checkConnectionOnConnect;
    }

    @JsonProperty
    public boolean getCheckConnectionOnReturn() {
        return this.checkConnectionOnReturn;
    }

    @JsonProperty
    public void setCheckConnectionOnReturn(final boolean checkConnectionOnReturn) {
        this.checkConnectionOnReturn = checkConnectionOnReturn;
    }

    @JsonProperty
    public Duration getEvictionInterval() {
        return this.evictionInterval;
    }

    @JsonProperty
    public void setEvictionInterval(final Duration interval) {
        this.evictionInterval = interval;
    }

    @JsonProperty
    public Duration getValidationInterval() {
        return this.validationInterval;
    }

    @JsonProperty
    public void setValidationInterval(final Duration validationInterval) {
        this.validationInterval = validationInterval;
    }

    @JsonProperty
    public Optional<String> getValidatorClassName() {
        return this.validatorClassName;
    }

    @JsonProperty
    public void setValidatorClassName(final Optional<String> validatorClassName) {
        this.validatorClassName = validatorClassName;
    }

    @Override
    @Deprecated
    @JsonIgnore
    public Optional<Duration> getHealthCheckValidationTimeout() {
        return this.getValidationQueryTimeout();
    }

    @JsonProperty
    public void setValidationQueryTimeout(final Duration validationQueryTimeout) {
        this.validationQueryTimeout = validationQueryTimeout;
    }

    @JsonProperty
    public boolean isRemoveAbandoned() {
        return this.removeAbandoned;
    }

    @JsonProperty
    public void setRemoveAbandoned(final boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    @JsonProperty
    public Duration getRemoveAbandonedTimeout() {
        return this.removeAbandonedTimeout;
    }

    @JsonProperty
    public void setRemoveAbandonedTimeout(final Duration removeAbandonedTimeout) {
        this.removeAbandonedTimeout = Objects.requireNonNull(removeAbandonedTimeout);
    }

    @Override
    public void asSingleConnectionPool() {
        this.minSize = 1;
        this.maxSize = 1;
    }

    @Override
    public ManagedDataSource build(final MetricRegistry metricRegistry, final String name) {
        final Properties properties = new Properties();
        for (final Map.Entry<String, String> property : this.properties.entrySet()) {
            properties.setProperty(property.getKey(), property.getValue());
        }

        final HikariConfig config = new HikariConfig();
        config.setDataSourceProperties(properties);
        if (this.autoCommitByDefault.isPresent()) {
            config.setAutoCommit(this.autoCommitByDefault.get());
        }
        config.setCatalog(this.defaultCatalog);
        if (this.readOnlyByDefault.isPresent()) {
            config.setReadOnly(this.readOnlyByDefault.get());
        }
        config.setDriverClassName(this.driverClass);
        config.setConnectionInitSql(this.initializationQuery);
        config.setMaximumPoolSize(this.maxSize);
        config.setMinimumIdle(this.minSize);
        if (this.getMaxConnectionAge().isPresent()) {
            config.setMaxLifetime(this.maxConnectionAge.toMilliseconds());
        }
        config.setPoolName(name);
        config.setJdbcUrl(this.url);
        config.setUsername(this.user);
        config.setPassword(this.user != null && this.password == null ? "" : this.password);
        config.setConnectionTestQuery(this.validationQuery);
        this.getValidationQueryTimeout().ifPresent(timeout -> config.setValidationTimeout(timeout.toMilliseconds()));
        if (this.defaultTransactionIsolation.isPresent()) {
            config.setTransactionIsolation(this.defaultTransactionIsolation.get().toString());
        }
        this.getConnectionTimeout().ifPresent(timeout -> config.setConnectionTimeout(timeout.toMilliseconds()));
        this.getIdleTimeout().ifPresent(timeout -> config.setIdleTimeout(timeout.toMilliseconds()));
        this.getInitializationFailTimeout()
                .ifPresent(timeout -> config.setInitializationFailTimeout(timeout.toMilliseconds()));
        this.getValidationTimeout().ifPresent(timeout -> config.setValidationTimeout(timeout.toMilliseconds()));
        return new ManagedPooledDataSource(config, metricRegistry);
    }

    @Override
    @JsonProperty
    public java.util.Optional<Duration> getValidationQueryTimeout() {
        return Optional.ofNullable(this.validationQueryTimeout);
    }
}
