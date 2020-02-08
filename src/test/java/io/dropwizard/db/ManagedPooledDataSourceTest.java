package io.dropwizard.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hibernate.SessionFactory;
import org.hibernate.annotations.DynamicUpdate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zaxxer.hikari.HikariDataSource;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.hibernate.AbstractDAO;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ManagedPooledDataSourceTest {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Table(name = "test")
    @Entity
    @DynamicUpdate
    public static class TestEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", unique = true, nullable = false)
        private int id;

        @Column(name = "`key`", unique = true, nullable = false, length = 736)
        private String key;
    }

    public static class TestEntityDAO extends AbstractDAO<TestEntity> {
        public TestEntityDAO(final SessionFactory sessionFactory) {
            super(sessionFactory);
        }

        public TestEntity getById(final int id) {
            return this.get(id);
        }

        public void save(final TestEntity entity) {
            this.persist(entity);
        }
    }

    @Path("/test")
    @AllArgsConstructor
    @Consumes
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestResource {
        private final TestEntityDAO dao;

        @GET
        @Path("/{id}")
        @UnitOfWork
        public TestEntity get(@PathParam("id") final int id) {
            return this.dao.getById(id);
        }

        @POST
        @UnitOfWork
        public Response post(final TestEntity entity) {
            this.dao.save(entity);
            return Response.created(URI.create(entity.getId() + "")).build();
        }
    }

    @Getter
    public static class SampleConfiguration extends Configuration {
        @Valid
        @NotNull
        @JsonProperty("database")
        private final DataSourceFactory database = new DataSourceFactory();
    }

    public static class SampleApplication extends Application<SampleConfiguration> {
        private final HibernateBundle<SampleConfiguration> hibernate = new HibernateBundle<SampleConfiguration>(
                TestEntity.class) {
            @Override
            public DataSourceFactory getDataSourceFactory(
                    final SampleConfiguration configuration) {
                final ManagedDataSource dataSource = configuration.getDatabase().build(new MetricRegistry(), "");
                assertThat(dataSource).isInstanceOf(HikariDataSource.class);
                final HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                assertThat(hikariDataSource.getConnectionTimeout()).isEqualTo(1000L);
                return configuration.getDatabase();
            }
        };

        @Override
        public void initialize(final Bootstrap<SampleConfiguration> bootstrap) {
            super.initialize(bootstrap);
            bootstrap.addBundle(this.hibernate);
        }

        @Override
        public void run(final SampleConfiguration configuration, final Environment environment)
                throws Exception {
            environment.jersey().register(
                    new TestResource(new TestEntityDAO(this.hibernate.getSessionFactory())));
        }
    }

    private final DropwizardTestSupport<SampleConfiguration> testSupport = new DropwizardTestSupport<>(
            SampleApplication.class, "src/test/resources/config.yml");
    private final DropwizardAppExtension<SampleConfiguration> DROPWIZARD = new DropwizardAppExtension<>(
            this.testSupport);

    @Test
    public void testInsertAndQuery() throws Exception {
        final TestEntity entity = TestEntity.builder()
                .key("abc").build();

        final Response response = this.DROPWIZARD.client()
                .target(String.format("http://localhost:%d/test", this.DROPWIZARD.getLocalPort()))
                .request()
                .post(javax.ws.rs.client.Entity.json(entity));
        assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

        final TestEntity createdEntity = this.DROPWIZARD.client()
                .target(String.format("http://localhost:%d/test/1", this.DROPWIZARD.getLocalPort()))
                .request()
                .get(TestEntity.class);
        assertThat(createdEntity).isEqualToIgnoringGivenFields(entity, "id");
    }
}