package zone.dragon.dropwizard.metrics;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Counted;
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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import zone.dragon.dropwizard.HK2Bundle;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class CounterFactoryTest {
    public static final DropwizardAppExtension<Configuration> RULE = new DropwizardAppExtension<>(CounterApp.class, new Configuration());

    public static class CounterApp extends Application<Configuration> {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            HK2Bundle.addTo(bootstrap);
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(CounterResource.class);
        }
    }

    @Path("/inc")
    @Singleton
    public static class CounterResource {
        @Inject
        public CounterResource(
            Counter unnamedCounter,
            @Metric(name = "com.metric") Counter metricCounter,
            @Metric(name = "com.absoluteMetric", absolute = true) Counter absoluteMetricCounter
        ) {
            unnamedCounter.inc();
            metricCounter.inc(2);
            absoluteMetricCounter.inc(3);
        }

        @GET
        @Counted(monotonic = true)
        public int increment() {
            return 6;
        }

        @PUT
        @Counted(monotonic = true)
        public int update(int input) {
            return input;
        }

    }

    protected JerseyWebTarget client = JerseyClientBuilder.createClient().target(String.format("http://localhost:%d", RULE.getLocalPort()));

    @Test
    public void testAbsoluteNamedCounterCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getCounters()).containsKey("com.absoluteMetric");
    }

    @Test
    public void testRelativeNamedCounterCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getCounters()).containsKey("zone.dragon.dropwizard.metrics.CounterFactoryTest.CounterResource.com.metric");
    }

    @Test
    public void testUnnamedCounterCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getCounters()).containsKey("zone.dragon.dropwizard.metrics.CounterFactoryTest.CounterResource.unnamedCounter");
    }

    @Test
    public void testCounterAnnotationIntercepted() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getCounters()).containsKey("zone.dragon.dropwizard.metrics.CounterFactoryTest.CounterResource.increment");
    }

    @Test
    public void testCounterAnnotationTagging() {
        int            result   = client.path("inc").request().put(Entity.entity("1", MediaType.APPLICATION_JSON_TYPE), Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getCounters()).containsKey("zone.dragon.dropwizard.metrics.CounterFactoryTest.CounterResource.update");
    }
}
