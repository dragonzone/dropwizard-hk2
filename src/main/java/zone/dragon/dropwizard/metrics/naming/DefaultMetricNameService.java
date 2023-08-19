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

package zone.dragon.dropwizard.metrics.naming;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.glassfish.hk2.api.IterableProvider;
import org.jvnet.hk2.annotations.Optional;

import lombok.NonNull;

/**
 * Builds metric names using all available {@link MetricNameFilter} and a {@link MetricNameFormatter}; Filters are processed descending rank
 * order, with highest rank running first and lowest rank running last.
 */
@Singleton
public class DefaultMetricNameService implements MetricNameService {
    private final IterableProvider<MetricNameFilter> filters;

    private final MetricNameFormatter formatter;

    @Inject
    public DefaultMetricNameService(@NonNull IterableProvider<MetricNameFilter> filters, @Optional MetricNameFormatter formatter) {
        this.filters = filters;
        this.formatter = formatter == null ? new DefaultMetricNameFormatter() : formatter;
    }

    @Override
    public MetricName getMetricName(AnnotatedElement injectionSite, Type metricType, String baseName) {
        MetricName name = MetricName.of(baseName);
        for (MetricNameFilter filter : filters) {
            name = filter.buildName(name, injectionSite, metricType);
        }
        return name;
    }

    @Override
    public String getFormattedMetricName(AnnotatedElement injectionSite, Type metricType, String baseName) {
        return formatter.formatName(getMetricName(injectionSite, metricType, baseName));
    }
}
