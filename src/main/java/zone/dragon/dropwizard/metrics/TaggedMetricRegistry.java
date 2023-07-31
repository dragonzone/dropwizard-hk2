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

import java.lang.reflect.Type;

import org.jvnet.hk2.annotations.Optional;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import lombok.NonNull;
import lombok.experimental.Delegate;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * Metric registry that supports contextual tagging of metrics. This wraps an existing {@link MetricRegistry} and adds tags to the names of
 * metrics as they are created by leveraging the {@link MetricNameService} to extract contextual tag information at the time the metric is
 * created. If no metric name service is available, no tags are added.
 */
public class TaggedMetricRegistry extends MetricRegistry {
    // Marker used to exclude the methods we want to override from @Delegate
    private interface ExcludedMethods {
        Counter counter(String name);

        Histogram histogram(String name);

        Meter meter(String name);

        <T extends Metric> T register(String name, T metric) throws IllegalArgumentException;

        void registerAll(MetricSet metrics) throws IllegalArgumentException;

        void registerAll(String prefix, MetricSet metrics);

        Timer timer(String name);
    }

    @Delegate(excludes = ExcludedMethods.class)
    private final MetricRegistry delegate;

    private final Provider<MetricNameService> metricNameServiceProvider;

    public TaggedMetricRegistry(@NonNull MetricRegistry registry, @NonNull MetricNameService metricNameService) {
        this(registry, () -> metricNameService);
    }

    @Inject
    public TaggedMetricRegistry(
        @NonNull MetricRegistry registry, @NonNull @Optional Provider<MetricNameService> metricNameServiceProvider
    ) {
        this.delegate = registry;
        this.metricNameServiceProvider = metricNameServiceProvider;
    }

    protected String getTaggedName(String baseName, Type type) {
        MetricNameService metricNameService = this.metricNameServiceProvider.get();
        if (metricNameService == null) {
            return baseName;
        }
        return metricNameService.getFormattedMetricName(null, type, baseName);
    }

    @Override
    public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException {
        if (metric instanceof MetricSet) {
            registerAll(name, (MetricSet) metric);
        } else {
            delegate.register(getTaggedName(name, metric.getClass()), metric);
        }
        return metric;
    }

    /**
     * Given a metric set, registers them.
     *
     * @param metrics
     *     a set of metrics
     *
     * @throws IllegalArgumentException
     *     if any of the names are already registered
     */
    @Override
    public void registerAll(MetricSet metrics) throws IllegalArgumentException {
        registerAll(null, metrics);
    }

    /**
     * Return the {@link Counter} registered under this name; or create and register a new {@link Counter} if none is registered.
     *
     * @param name
     *     the name of the metric
     *
     * @return a new or pre-existing {@link Counter}
     */
    @Override
    public Counter counter(String name) {
        return delegate.counter(getTaggedName(name, Counter.class));
    }

    /**
     * Return the {@link Histogram} registered under this name; or create and register a new {@link Histogram} if none is registered.
     *
     * @param name
     *     the name of the metric
     *
     * @return a new or pre-existing {@link Histogram}
     */
    @Override
    public Histogram histogram(String name) {
        return delegate.histogram(getTaggedName(name, Histogram.class));
    }

    /**
     * Return the {@link Meter} registered under this name; or create and register a new {@link Meter} if none is registered.
     *
     * @param name
     *     the name of the metric
     *
     * @return a new or pre-existing {@link Meter}
     */
    @Override
    public Meter meter(String name) {
        return delegate.meter(getTaggedName(name, Meter.class));
    }

    /**
     * Return the {@link Timer} registered under this name; or create and register a new {@link Timer} if none is registered.
     *
     * @param name
     *     the name of the metric
     *
     * @return a new or pre-existing {@link Timer}
     */
    @Override
    public Timer timer(String name) {
        return delegate.timer(getTaggedName(name, Timer.class));
    }

    @Override
    public void registerAll(String prefix, MetricSet metricSet) {
        metricSet.getMetrics().forEach((key, value) -> register(name(prefix, key), value));
    }
}
