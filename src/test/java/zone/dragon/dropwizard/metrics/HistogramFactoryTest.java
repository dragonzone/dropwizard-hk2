package zone.dragon.dropwizard.metrics;

import com.codahale.metrics.Histogram;
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

public class HistogramFactoryTest {
    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE = new DropwizardAppRule<>(HistogramApp.class, new Configuration());

    public static class HistogramApp extends Application<Configuration> {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            HK2Bundle.addTo(bootstrap);
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(HistogramResource.class);
        }
    }

    @Path("/inc")
    @Singleton
    public static class HistogramResource {
        @Inject
        public HistogramResource(
            Histogram unnamedHistogram,
            @Metric(name = "com.metric") Histogram metricHistogram,
            @Metric(name = "com.absoluteMetric", absolute = true) Histogram absoluteMetricHistogram
        ) {
            unnamedHistogram.update(123);
            metricHistogram.update(456);
            absoluteMetricHistogram.update(789);
        }

        @GET
        public int increment() {
            return 6;
        }
    }

    protected JerseyWebTarget client = JerseyClientBuilder.createClient().target(String.format("http://localhost:%d", RULE.getLocalPort()));

    @Test
    public void testAbsoluteNamedHistogramCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getHistograms()).containsKey("com.absoluteMetric");
    }

    @Test
    public void testRelativeNamedHistogramCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getHistograms()).containsKey("zone.dragon.dropwizard.metrics.HistogramFactoryTest.HistogramResource.com.metric");
    }

    @Test
    public void testUnnamedHistogramCreated() {
        int            result   = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getHistograms()).containsKey("zone.dragon.dropwizard.metrics.HistogramFactoryTest.HistogramResource.arg0");
    }
}
