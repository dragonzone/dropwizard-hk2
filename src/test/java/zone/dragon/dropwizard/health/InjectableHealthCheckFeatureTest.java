package zone.dragon.dropwizard.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codahale.metrics.health.HealthCheck.Result;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import zone.dragon.dropwizard.TestApplication;
import zone.dragon.dropwizard.TestConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Bryan Harclerode
 * @date 9/23/2016
 */
@ExtendWith(DropwizardExtensionsSupport.class)
public class InjectableHealthCheckFeatureTest {
    public static final DropwizardAppExtension<TestConfig> RULE = new DropwizardAppExtension<>(
        TestApplication.class,
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
