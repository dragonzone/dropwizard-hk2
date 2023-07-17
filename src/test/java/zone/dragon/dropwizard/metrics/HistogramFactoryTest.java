package zone.dragon.dropwizard.metrics;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codahale.metrics.Histogram;
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
public class HistogramFactoryTest {
    public static final DropwizardAppExtension<Configuration> RULE = new DropwizardAppExtension<>(HistogramApp.class, new Configuration());

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
        int result = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getHistograms()).containsKey("com.absoluteMetric");
    }

    @Test
    public void testRelativeNamedHistogramCreated() {
        int result = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getHistograms()).containsKey("zone.dragon.dropwizard.metrics.HistogramFactoryTest.HistogramResource.com.metric");
    }

    @Test
    public void testUnnamedHistogramCreated() {
        int result = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getHistograms()).containsKey(
            "zone.dragon.dropwizard.metrics.HistogramFactoryTest.HistogramResource.unnamedHistogram");
    }
}
