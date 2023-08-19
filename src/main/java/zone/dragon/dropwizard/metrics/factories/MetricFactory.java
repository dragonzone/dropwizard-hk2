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

package zone.dragon.dropwizard.metrics.factories;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Function;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InstantiationService;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.Visibility;

import com.codahale.metrics.Metric;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * Factory that handles injecting metrics named based on the injection site
 */
@Singleton
@Slf4j
public class MetricFactory<T extends Metric> implements Factory<T> {
    private final InstantiationService instantiationService;

    private final MetricNameService nameService;

    private final Function<String, T> metricSupplier;

    @Inject
    public MetricFactory(
        @NonNull InstantiationService instantiationService,
        @NonNull MetricNameService metricNameService,
        @NonNull Function<String, T> metricSupplier
    ) {
        this.instantiationService = instantiationService;
        this.nameService = metricNameService;
        this.metricSupplier = metricSupplier;
    }

    @Override
    @PerLookup
    @Visibility(DescriptorVisibility.LOCAL)
    public T provide() {
        Injectee injectee = instantiationService.getInstantiationData().getParentInjectee();
        String name;
        if (injectee == null) {
            log.warn("Creating metric with no injection context; Use the metric registry directly instead of dynamically creating it "
                + "through HK2", new Exception());
            name = UUID.randomUUID().toString();
        } else {
            AnnotatedElement parent = injectee.getParent();
            if (parent instanceof Constructor) {
                parent = ((Constructor) parent).getParameters()[injectee.getPosition()];
            }
            if (parent instanceof Method) {
                parent = ((Method) parent).getParameters()[injectee.getPosition()];
            }
            name = nameService.getFormattedMetricName(parent, injectee.getInjecteeClass());
        }
        return metricSupplier.apply(name);
    }

    @Override
    public void dispose(T instance) {
        //does nothing
    }
}
