package zone.dragon.dropwizard;

import org.eclipse.jetty.server.Server;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Bryan Harclerode
 */
@ExtendWith(DropwizardExtensionsSupport.class)
public class HK2BundleTest {
    public static final DropwizardAppExtension<TestConfig> RULE = new DropwizardAppExtension<>(HK2BundleApp.class,
                                                                                     ResourceHelpers.resourceFilePath("config.yaml")
    );
    private static HK2Bundle<TestConfig> bundle;
    private static ServiceLocator        jerseyLocator;

    public static class BundleWithBinder implements ConfiguredBundle<TestConfig>, SimpleBinder {
        @Override
        public void initialize(Bootstrap<?> bootstrap) {
        }

        @Override
        public void run(TestConfig config, Environment environment) {
        }

        @Override
        public void configureBindings(AbstractBinder config) {
            config.bind("fromBundle").to(String.class).named("bundleString");
        }
    }

    public static class ExtractLocatorFeature implements Feature {
        @Inject
        public ExtractLocatorFeature(ServiceLocator locator) {
            jerseyLocator = locator;
        }

        @Override
        public boolean configure(FeatureContext context) {
            return true;
        }
    }

    public static class HK2BundleApp extends Application<TestConfig> {
        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            bundle = HK2Bundle.addTo(bootstrap);
            bootstrap.addBundle(new BundleWithBinder());
            bundle.bind("test1").to(String.class).named("parentBinding");
            bundle.bindAsContract(TestService.class).in(RequestScoped.class);
        }

        @Override
        public void run(TestConfig testConfig, Environment environment) throws Exception {
            environment.jersey().register(ExtractLocatorFeature.class);
            environment.jersey().register(TestResource.class);
            bundle.bind("appRun").to(String.class).named("appRunBinding");
        }
    }

    @Path("/test")
    public static class TestResource {
        @GET
        public String resourceTest(@Context TestService injected) {
            return injected.getValue();
        }
    }

    public static class TestService {
        @Inject
        public TestService() {}

        public String getValue() {
            return "test2";
        }
    }

    @Test
    public void testApplicationBound() {
        assertThat(jerseyLocator.getService(Application.class)).isSameAs(RULE.getApplication());
    }

    @Test
    public void testApplicationRunBindings() {
        assertThat(jerseyLocator.getService(String.class, "appRunBinding")).isEqualTo("appRun");
    }

    @Test
    public void testBundleBound() {
        assertThat(jerseyLocator.getService(String.class, "bundleString")).isEqualTo("fromBundle");
    }

    @Test
    public void testConfigurationBound() {
        assertThat(jerseyLocator.getService(Configuration.class)).isSameAs(RULE.getConfiguration());
        assertThat(jerseyLocator.getService(TestConfig.class)).isSameAs(RULE.getConfiguration());
    }

    @Test
    public void testEnvironmentBound() {
        assertThat(jerseyLocator.getService(Environment.class)).isSameAs(RULE.getEnvironment());
    }

    @Test
    public void testHealthCheckRegistryBound() {
        assertThat(jerseyLocator.getService(HealthCheckRegistry.class)).isSameAs(RULE.getEnvironment().healthChecks());
    }

    @Test
    public void testLifecycleEnvironmentBound() {
        assertThat(jerseyLocator.getService(LifecycleEnvironment.class)).isSameAs(RULE.getEnvironment().lifecycle());
    }

    @Test
    public void testLocatorFound() {
        assertThat(jerseyLocator).isNotNull();
    }

    @Test
    public void testMetricRegistryBound() {
        assertThat(jerseyLocator.getService(MetricRegistry.class)).isSameAs(RULE.getEnvironment().metrics());
    }

    @Test
    public void testObjectMapperBound() {
        assertThat(jerseyLocator.getService(ObjectMapper.class)).isSameAs(RULE.getEnvironment().getObjectMapper());
    }

    @Test
    public void testParentPreBinding() {
        assertThat(jerseyLocator.getService(String.class, "parentBinding")).isEqualTo("test1");
    }

    @Test
    public void testRequestScopedFromParent() {
        Client    client   = JerseyClientBuilder.newClient();
        WebTarget target   = client.target("http://localhost:" + RULE.getLocalPort());
        String    response = target.path("test").request().get(String.class);
        assertThat(response).isEqualTo("test2");
    }

    @Test
    public void testServerBound() {
        assertThat(jerseyLocator.getService(Server.class)).isNotNull();
    }

    @Test
    public void testValidatorBound() {
        assertThat(jerseyLocator.getService(Validator.class)).isSameAs(RULE.getEnvironment().getValidator());
    }
}
