package zone.dragon.dropwizard.metrics;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.ClassRule;
import org.junit.Test;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metric;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import zone.dragon.dropwizard.HK2Bundle;

import static org.assertj.core.api.Assertions.assertThat;

public class TimerFactoryTest {
    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE = new DropwizardAppRule<>(TimerApp.class, new Configuration());

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
        assertThat(registry.getTimers()).containsKey("zone.dragon.dropwizard.metrics.TimerFactoryTest.TimerResource.arg0");
    }

    @Test
    public void testTimerAnnotationIntercepted() throws JsonProcessingException {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        System.out.println(RULE.getObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(registry));
        assertThat(registry.getTimers()).containsKey("zone.dragon.dropwizard.metrics.TimerFactoryTest.TimerResource");
    }
}
