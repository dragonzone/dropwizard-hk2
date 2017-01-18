package zone.dragon.dropwizard;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.junit.ClassRule;
import org.junit.Test;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Darth Android
 * @date 1/10/2017
 */
public class HK2BundleTest {
    @ClassRule
    public static final DropwizardAppRule<TestConfig> RULE = new DropwizardAppRule<>(HK2BundleApp.class,
                                                                                     ResourceHelpers.resourceFilePath("config.yaml")
    );
    private static HK2Bundle<TestConfig> bundle;
    private static ServiceLocator        jerseyLocator;

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
            bootstrap.addBundle(bundle = new HK2Bundle<>());
            bundle.bind("test1").to(String.class).named("parentBinding");
            bundle.bindAsContract(TestService.class).in(RequestScoped.class);
        }

        @Override
        public void run(TestConfig testConfig, Environment environment) throws Exception {
            environment.jersey().register(ExtractLocatorFeature.class);
            environment.jersey().register(TestResource.class);
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
    public void testLocatorFound() {
        assertThat(jerseyLocator).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void testParentPostBinding() {
        bundle.bind("testPost");
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
}
