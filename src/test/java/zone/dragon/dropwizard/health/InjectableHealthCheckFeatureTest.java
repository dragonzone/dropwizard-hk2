package zone.dragon.dropwizard.health;

import org.junit.ClassRule;
import org.junit.Test;

import com.codahale.metrics.health.HealthCheck.Result;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import zone.dragon.dropwizard.TestApplication;
import zone.dragon.dropwizard.TestConfig;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(RULE.getEnvironment().lifecycle()).isNotNull();
    }

    @Test
    public void testHealthCheckCreated() {
        assertThat(RULE.getEnvironment().healthChecks().getNames()).hasSize(2);
        final Result expected = Result.healthy("testValue");
        final Result result = RULE.getEnvironment().healthChecks().runHealthCheck("TestHealthCheck");
        assertThat(result.isHealthy()).isEqualTo(expected.isHealthy());
        assertThat(result.getMessage()).isEqualTo(expected.getMessage());
        assertThat(result.getDetails()).isEqualTo(expected.getDetails());
        // Ignore timestamp differences
    }
}
