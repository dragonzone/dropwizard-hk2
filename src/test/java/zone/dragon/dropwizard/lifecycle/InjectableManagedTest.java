package zone.dragon.dropwizard.lifecycle;

import org.junit.jupiter.api.AfterAll;
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
public class InjectableManagedTest {
    private static String testValue;

    private static boolean stopped;

    @AfterAll
    public static void verifyStop() {
        assertThat(stopped).isEqualTo(true);
    }

    public static class ManagedApp extends Application<TestConfig> {
        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            HK2Bundle.addTo(bootstrap);
        }

        @Override
        public void run(TestConfig testConfig, Environment environment) throws Exception {
            environment.jersey().register(TestManaged.class);
        }
    }

    public static class TestManaged implements InjectableManaged {
        private final TestConfig config;

        @Inject
        public TestManaged(TestConfig config) {
            this.config = config;
        }

        @Override
        public void start() throws Exception {
            if (testValue != null) {
                fail("Already started");
            }
            testValue = config.getTestProperty();
        }

        @Override
        public void stop() throws Exception {
            if (stopped) {
                fail("Already stopped");
            }
            stopped = true;
        }
    }

    public final DropwizardAppExtension<TestConfig> RULE = new DropwizardAppExtension<>(
        ManagedApp.class,
        ResourceHelpers.resourceFilePath("config.yaml")
    );

    @Test
    public void testServerStartedCalled() {
        assertThat(testValue).isEqualTo("testValue");
    }
}
