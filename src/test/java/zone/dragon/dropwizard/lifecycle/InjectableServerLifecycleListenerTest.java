package zone.dragon.dropwizard.lifecycle;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.eclipse.jetty.server.Server;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import zone.dragon.dropwizard.HK2Bundle;
import zone.dragon.dropwizard.TestConfig;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Bryan Harclerode
 * Date 10/29/2016
 */
public class InjectableServerLifecycleListenerTest {
    @ClassRule
    public static final DropwizardAppRule<TestConfig> RULE = new DropwizardAppRule<>(SLLApp.class,
                                                                                     ResourceHelpers.resourceFilePath("config.yaml")
    );
    private static String testValue;

    public static class SLLApp extends Application<TestConfig> {
        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            bootstrap.addBundle(new HK2Bundle<>());
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
                Assert.fail("Invoked twice!");
            }
            testValue = config.getTestProperty();
        }
    }

    @Test
    public void testServerStartedCalled() {
        assertThat(testValue).isEqualTo("testValue");
    }
}
