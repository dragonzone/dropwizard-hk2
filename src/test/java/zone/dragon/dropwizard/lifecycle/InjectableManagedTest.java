package zone.dragon.dropwizard.lifecycle;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import zone.dragon.dropwizard.HK2Bundle;
import zone.dragon.dropwizard.TestConfig;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Bryan Harclerode
 * Date 10/29/2016
 */
public class InjectableManagedTest {
    @Rule
    public final DropwizardAppRule<TestConfig> RULE = new DropwizardAppRule<>(ManagedApp.class,
                                                                                     ResourceHelpers.resourceFilePath("config.yaml")
    );
    private static String testValue;
    private static boolean stopped;

    public static class ManagedApp extends Application<TestConfig> {
        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            bootstrap.addBundle(new HK2Bundle<>());
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
                Assert.fail("Already started");
            }
            testValue = config.getTestProperty();
        }

        @Override
        public void stop() throws Exception {
            if (stopped) {
                Assert.fail("Already stopped");
            }
            stopped = true;
        }
    }

    @Test
    public void testServerStartedCalled() {
        assertThat(testValue).isEqualTo("testValue");
    }

    @AfterClass
    public static void verifyStop() {
        assertThat(stopped).isEqualTo(true);
    }
}
