package zone.dragon.dropwizard.lifecycle;

import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.inject.Inject;
import zone.dragon.dropwizard.HK2Bundle;
import zone.dragon.dropwizard.TestConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Bryan Harclerode
 * @date 10/29/2016
 */
@ExtendWith(DropwizardExtensionsSupport.class)
public class InjectableServerLifecycleListenerTest {
    public static final DropwizardAppExtension<TestConfig> RULE = new DropwizardAppExtension<>(
        SLLApp.class,
        ResourceHelpers.resourceFilePath("config.yaml")
    );

    private static String testValue;

    public static class SLLApp extends Application<TestConfig> {
        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            HK2Bundle.addTo(bootstrap);
        }

        @Override
        public void run(TestConfig testConfig, Environment environment) throws Exception {
            environment.jersey().register(TestServerLifecycleListener.class);
        }
    }

    public static class TestServerLifecycleListener implements InjectableServerLifecycleListener {
        private final TestConfig config;

        @Inject
        public TestServerLifecycleListener(TestConfig config) {
            this.config = config;
        }

        @Override
        public void serverStarted(Server server) {
            if (testValue != null) {
                fail("Invoked twice!");
            }
            testValue = config.getTestProperty();
        }
    }

    @Test
    public void testServerStartedCalled() {
        assertThat(testValue).isEqualTo("testValue");
    }
}
