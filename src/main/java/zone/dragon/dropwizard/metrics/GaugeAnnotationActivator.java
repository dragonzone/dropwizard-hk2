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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Gauge;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import zone.dragon.dropwizard.MethodAnnotationActivator;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * When a {@link Singleton singleton} is activated, any parameterless methods annotated with {@link Gauge @Gauge} will be used to provide
 * values for a {@link com.codahale.metrics.Gauge Gauge}
 */
@Slf4j
@Singleton
public class GaugeAnnotationActivator extends MethodAnnotationActivator<Gauge> {
    private final MetricRegistry metricRegistry;

    private final MetricNameService metricNameService;

    @Inject
    public GaugeAnnotationActivator(@NonNull MetricRegistry metricRegistry, @NonNull MetricNameService metricNameService) {
        super(Gauge.class);
        this.metricRegistry = metricRegistry;
        this.metricNameService = metricNameService;
    }

    @Override
    protected void activate(ActiveDescriptor<?> handle, Object service, Method method, Gauge annotation) {
        if (method.getParameterCount() != 0) {
            log.error("@Gauge placed on method {} which must have zero parameters, but has {}", method, method.getParameterCount());
            return;
        }
        com.codahale.metrics.Gauge<?> gauge = () -> {
            try {
                return method.invoke(service);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read gauge value from " + method, e instanceof InvocationTargetException ? (
                    (InvocationTargetException) e
                ).getTargetException() : e);
            }
        };
        String name = metricNameService.getFormattedMetricName(method, com.codahale.metrics.Gauge.class);
        log.debug("Activating gauge {} monitoring {}", name, method);
        metricRegistry.register(name, gauge);
    }
}
