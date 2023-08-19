/*
 * MIT License
 *
 * Copyright (c) 2016-2023 Bryan Harclerode
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package zone.dragon.dropwizard.metrics;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

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
        int result = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getCounters()).containsKey("com.absoluteMetric");
    }

    @Test
    public void testRelativeNamedCounterCreated() {
        int result = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getCounters()).containsKey("zone.dragon.dropwizard.metrics.CounterFactoryTest.CounterResource.com.metric");
    }

    @Test
    public void testUnnamedCounterCreated() {
        int result = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getCounters()).containsKey("zone.dragon.dropwizard.metrics.CounterFactoryTest.CounterResource.unnamedCounter");
    }

    @Test
    public void testCounterAnnotationIntercepted() {
        int result = client.path("inc").request().get(Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getCounters()).containsKey("zone.dragon.dropwizard.metrics.CounterFactoryTest.CounterResource.increment");
    }

    @Test
    public void testCounterAnnotationTagging() {
        int result = client.path("inc").request().put(Entity.entity("1", MediaType.APPLICATION_JSON_TYPE), Integer.class);
        MetricRegistry registry = RULE.getEnvironment().metrics();
        assertThat(registry.getCounters()).containsKey("zone.dragon.dropwizard.metrics.CounterFactoryTest.CounterResource.update");
    }
}
