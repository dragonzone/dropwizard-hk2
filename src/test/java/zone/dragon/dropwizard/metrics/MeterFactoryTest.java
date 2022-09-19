package zone.dragon.dropwizard.metrics;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metric;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import zone.dragon.dropwizard.HK2Bundle;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class MeterFactoryTest {
    public static final DropwizardAppExtension<Configuration> RULE = new DropwizardAppExtension<>(MeterApp.class, new Configuration());

    public static class MeterApp extends Application<Configuration> {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            HK2Bundle.addTo(bootstrap);
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(MeterResource.class);
        }
    }

    @Path("/inc")
    @Singleton
    public static class MeterResource {
        @Inject
        public MeterResource(
            Meter unnamedMeter,
            @Metric(name = "com.metric") Meter metricMeter,
            @Metric(name = "com.absoluteMetric", absolute = true) Meter absoluteMetricMeter
        ) {
            unnamedMeter.mark(123);
            metricMeter.mark(456);
            absoluteMetricMeter.mark(789);
        }

        @GET
        public int increment() {
            return 6;
        }
    }

    protected JerseyWebTarget client = JerseyClientBuilder.createClient().target(String.format("http://localhost:%d", RULE.getLocalPort()));

    @Test
    public void testAbsoluteNamedMeterCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getMeters()).containsKey("com.absoluteMetric");
    }

    @Test
    public void testRelativeNamedMeterCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getMeters()).containsKey("zone.dragon.dropwizard.metrics.MeterFactoryTest.MeterResource.com.metric");
    }

    @Test
    public void testUnnamedMeterCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getMeters()).containsKey("zone.dragon.dropwizard.metrics.MeterFactoryTest.MeterResource.unnamedMeter");
    }
}
