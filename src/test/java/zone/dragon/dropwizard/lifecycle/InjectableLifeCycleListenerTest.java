package zone.dragon.dropwizard.lifecycle;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.eclipse.jetty.util.component.LifeCycle;
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
 * @date 10/29/2016
 */
public class InjectableLifeCycleListenerTest {
    private static String testValue;
    private static boolean started;
    private static boolean stopping;
    private static boolean stopped;

    @AfterClass
    public static void verifyStopped() {
        assertThat(stopped).isEqualTo(true);
    }

    public static class LCLApp extends Application<TestConfig> {
        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            bootstrap.addBundle(new HK2Bundle<>());
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
                Assert.fail("Invoked twice!");
            }
            testValue = config.getTestProperty();
        }

        @Override
        public void lifeCycleStarted(LifeCycle event) {
            if (testValue == null) {
                Assert.fail("Starting event not called");
            }
            if (started) {
                Assert.fail("Already started");
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
                Assert.fail("Started event not called");
            }
            if (stopping) {
                Assert.fail("Already stopping");
            }
            stopping = true;
        }

        @Override
        public void lifeCycleStopped(LifeCycle event) {
            if (!stopping) {
                Assert.fail("Stopping event not called");
            }
            if (stopped) {
                Assert.fail("Already stopped");
            }
            stopped = true;
        }
    }

    @Rule
    public final DropwizardAppRule<TestConfig> RULE = new DropwizardAppRule<>(LCLApp.class,
                                                                              ResourceHelpers.resourceFilePath("config.yaml")
    );

    @Test
    public void testServerStartedCalled() {
        assertThat(testValue).isEqualTo("testValue");
    }
}
