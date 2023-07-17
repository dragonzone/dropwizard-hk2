package zone.dragon.dropwizard.lifecycle;

import org.eclipse.jetty.util.component.LifeCycle;
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
public class InjectableLifeCycleListenerTest {
    private static String testValue;

    private static boolean started;

    private static boolean stopping;

    private static boolean stopped;

    @AfterAll
    public static void verifyStopped() {
        assertThat(stopped).isEqualTo(true);
    }

    public static class LCLApp extends Application<TestConfig> {
        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            HK2Bundle.addTo(bootstrap);
        }

        @Override
        public void run(TestConfig testConfig, Environment environment) throws Exception {
            environment.jersey().register(TestLifeCycleListener.class);
        }
    }

    public static class TestLifeCycleListener implements InjectableLifeCycleListener {
        private final TestConfig config;

        @Inject
        public TestLifeCycleListener(TestConfig config) {
            this.config = config;
        }

        @Override
        public void lifeCycleStarting(LifeCycle event) {
            if (testValue != null) {
                fail("Invoked twice!");
            }
            testValue = config.getTestProperty();
        }

        @Override
        public void lifeCycleStarted(LifeCycle event) {
            if (testValue == null) {
                fail("Starting event not called");
            }
            if (started) {
                fail("Already started");
            }
            started = true;
        }

        @Override
        public void lifeCycleFailure(LifeCycle event, Throwable cause) {
            //not used
        }

        @Override
        public void lifeCycleStopping(LifeCycle event) {
            if (!started) {
                fail("Started event not called");
            }
            if (stopping) {
                fail("Already stopping");
            }
            stopping = true;
        }

        @Override
        public void lifeCycleStopped(LifeCycle event) {
            if (!stopping) {
                fail("Stopping event not called");
            }
            if (stopped) {
                fail("Already stopped");
            }
            stopped = true;
        }
    }

    public final DropwizardAppExtension<TestConfig> RULE = new DropwizardAppExtension<>(
        LCLApp.class,
        ResourceHelpers.resourceFilePath("config.yaml")
    );

    @Test
    public void testServerStartedCalled() {
        assertThat(testValue).isEqualTo("testValue");
    }
}
