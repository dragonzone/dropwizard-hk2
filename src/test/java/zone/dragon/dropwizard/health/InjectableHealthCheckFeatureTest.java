package zone.dragon.dropwizard.health;

import com.codahale.metrics.health.HealthCheck.Result;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import zone.dragon.dropwizard.TestApplication;
import zone.dragon.dropwizard.TestConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Bryan Harclerode
 * @date 9/23/2016
 */
public class InjectableHealthCheckFeatureTest {

    @ClassRule
    public static final DropwizardAppRule<TestConfig> RULE = new DropwizardAppRule<>(TestApplication.class,
                                                                                     ResourceHelpers.resourceFilePath("config.yaml")
    );

    @Test
    public void test() {
        assertTrue(RULE.getEnvironment().lifecycle() != null);
    }

    @Test
    public void testHealthCheckCreated() throws InterruptedException {
        Thread.sleep(1000);
        assertEquals(2, RULE.getEnvironment().healthChecks().getNames().size());
        assertEquals(Result.healthy(), RULE.getEnvironment().healthChecks().runHealthCheck("TestHealthCheck"));
    }

}
