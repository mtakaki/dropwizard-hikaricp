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
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hibernate.SessionFactory;
import org.hibernate.annotations.DynamicUpdate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.hibernate.AbstractDAO;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Rule
    public final DropwizardAppRule<SampleConfiguration> RULE = new DropwizardAppRule<>(
            SampleApplication.class,
            ResourceHelpers.resourceFilePath("config.yml"));
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Client client;

    @Before
    public void createClient() {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setTimeout(Duration.minutes(1L));
        configuration.setConnectionTimeout(Duration.minutes(1L));
        configuration.setConnectionRequestTimeout(Duration.minutes(1L));
        this.client = new JerseyClientBuilder(this.RULE.getEnvironment()).using(configuration)
                .build("test client");
    }

    @Test
    public void testInsertAndQuery() throws Exception {
        final TestEntity entity = TestEntity.builder()
                .key("abc").build();

        final Response response = this.client
                .target(String.format("http://localhost:%d/test", this.RULE.getLocalPort()))
                .request()
                .post(javax.ws.rs.client.Entity.json(entity));
        assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

        final TestEntity createdEntity = this.client
                .target(String.format("http://localhost:%d/test/1", this.RULE.getLocalPort()))
                .request()
                .get(TestEntity.class);
        assertThat(createdEntity).isEqualToIgnoringGivenFields(entity, "id");
    }
}