package zone.dragon.dropwizard.metrics;

import java.util.concurrent.TimeUnit;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metric;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;

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
public class TimerFactoryTest {
    public static final DropwizardAppExtension<Configuration> RULE = new DropwizardAppExtension<>(TimerApp.class, new Configuration());

    public static class TimerApp extends Application<Configuration> {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.getObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.SECONDS, false, MetricFilter.ALL));
            HK2Bundle.addTo(bootstrap);
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(TimerResource.class);
        }
    }

    @Path("/inc")
    @Singleton
    public static class TimerResource {
        @Inject
        @Timed
        public TimerResource(
            Timer unnamedTimer,
            @Metric(name = "com.metric") Timer metricTimer,
            @Metric(name = "com.absoluteMetric", absolute = true) Timer absoluteMetricTimer
        ) {
            unnamedTimer.update(123, TimeUnit.DAYS);
            metricTimer.update(123, TimeUnit.MICROSECONDS);
            absoluteMetricTimer.update(123, TimeUnit.MILLISECONDS);
        }

        @GET
        @Timed
        public int increment() {
            return 6;
        }
    }

    protected JerseyWebTarget client = JerseyClientBuilder.createClient().target(String.format("http://localhost:%d", RULE.getLocalPort()));

    @Test
    public void testAbsoluteNamedTimerCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getTimers()).containsKey("com.absoluteMetric");
    }

    @Test
    public void testRelativeNamedTimerCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getTimers()).containsKey("zone.dragon.dropwizard.metrics.TimerFactoryTest.TimerResource.com.metric");
    }

    @Test
    public void testUnnamedTimerCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getTimers()).containsKey("zone.dragon.dropwizard.metrics.TimerFactoryTest.TimerResource.unnamedTimer");
    }

    @Test
    public void testTimerAnnotationIntercepted() throws JsonProcessingException {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        System.out.println(RULE.getObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(registry));
        assertThat(registry.getTimers()).containsKey("zone.dragon.dropwizard.metrics.TimerFactoryTest.TimerResource");
    }
}
