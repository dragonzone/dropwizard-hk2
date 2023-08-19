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

import javax.inject.Singleton;

import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import zone.dragon.dropwizard.AnnotatedConstructorInterceptorFactory;
import zone.dragon.dropwizard.AnnotatedMethodInterceptorFactory;
import zone.dragon.dropwizard.AnnotationInterceptionService;
import zone.dragon.dropwizard.metrics.interceptors.CountedInterceptorFactory;
import zone.dragon.dropwizard.metrics.interceptors.ExceptionMeteredInterceptorFactory;
import zone.dragon.dropwizard.metrics.interceptors.MeteredInterceptorFactory;
import zone.dragon.dropwizard.metrics.interceptors.TimedInterceptorFactory;
import zone.dragon.dropwizard.metrics.naming.DefaultMetricNameFormatter;
import zone.dragon.dropwizard.metrics.naming.DefaultMetricNameService;
import zone.dragon.dropwizard.metrics.naming.MetricNameFilter;
import zone.dragon.dropwizard.metrics.naming.MetricNameFormatter;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;
import zone.dragon.dropwizard.metrics.naming.filters.CodahaleMetricNameFilter;

/**
 * Binder that registers all of the metric components with HK2; It is expected that the {@link MetricRegistry} and
 * {@link AnnotationInterceptionService} are already bound in HK2.
 */
public class HK2MetricBinder extends AbstractBinder {
    @Override
    protected void configure() {
        // Binding naming services
        bind(DefaultMetricNameService.class).to(MetricNameService.class).in(Singleton.class);
        bind(DefaultMetricNameFormatter.class).to(MetricNameFormatter.class).in(Singleton.class);
        bind(CodahaleMetricNameFilter.class).to(MetricNameFilter.class).in(Singleton.class).ranked(MetricNameFilter.DEFAULT_NAME_PRIORITY);
        bindAsContract(TaggedMetricRegistry.class).in(Singleton.class);
        // Bind Metric method handlers
        bind(GaugeAnnotationActivator.class).to(InstanceLifecycleListener.class).in(Singleton.class);
        bind(CachedGaugeAnnotationActivator.class).to(InstanceLifecycleListener.class).in(Singleton.class);
        // Bind Metric  interceptors
        bind(CountedInterceptorFactory.class)
            .to(new TypeLiteral<AnnotatedMethodInterceptorFactory<Counted>>() {})
            .to(new TypeLiteral<AnnotatedConstructorInterceptorFactory<Counted>>() {})
            .in(Singleton.class);
        bind(ExceptionMeteredInterceptorFactory.class)
            .to(new TypeLiteral<AnnotatedMethodInterceptorFactory<ExceptionMetered>>() {})
            .to(new TypeLiteral<AnnotatedConstructorInterceptorFactory<ExceptionMetered>>() {})
            .in(Singleton.class);
        bind(MeteredInterceptorFactory.class)
            .to(new TypeLiteral<AnnotatedMethodInterceptorFactory<Metered>>() {})
            .to(new TypeLiteral<AnnotatedConstructorInterceptorFactory<Metered>>() {})
            .in(Singleton.class);
        bind(TimedInterceptorFactory.class)
            .to(new TypeLiteral<AnnotatedMethodInterceptorFactory<Timed>>() {})
            .to(new TypeLiteral<AnnotatedConstructorInterceptorFactory<Timed>>() {})
            .in(Singleton.class);
    }
}
