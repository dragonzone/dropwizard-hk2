package zone.dragon.dropwizard.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metric;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.ClassRule;
import org.junit.Test;
import zone.dragon.dropwizard.HK2Bundle;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class CounterFactoryTest {
    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE = new DropwizardAppRule<>(App.class, new Configuration());

    public static class App extends Application<Configuration> {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            HK2Bundle.addTo(bootstrap);
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(Resource.class);
        }
    }

    @Path("/inc")
    @Singleton
    public static class Resource {
        private final Counter unnamedCounter;

        @Inject
        public Resource(Counter unnamedCounter, @Metric(name = "con.metric") Counter metricCounter) {
            this.unnamedCounter = unnamedCounter;
            unnamedCounter.inc();
            metricCounter.inc(2);
        }

        @GET
        public int increment() {
            return 6;
        }
    }

    protected JerseyWebTarget client = JerseyClientBuilder.createClient().target(String.format("http://localhost:%d", RULE.getLocalPort()));

    @Test
    public void testUnnamedCounterCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getCounters()).containsKey("zone.dragon.dropwizard.metrics.CounterFactoryTest.Resource.arg0");
        assertThat(registry
                       .getCounters()
                       .get("zone.dragon.dropwizard.metrics.CounterFactoryTest.Resource.arg0")
                       .getCount()).isEqualTo(1);
    }

    @Test
    public void testRelativeNamedCounterCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getCounters()).containsKey("zone.dragon.dropwizard.metrics.CounterFactoryTest.Resource.con.metric");
        assertThat(registry.getCounters().get("zone.dragon.dropwizard.metrics.CounterFactoryTest.Resource.con.metric").getCount()).isEqualTo
            (2);
    }
}
