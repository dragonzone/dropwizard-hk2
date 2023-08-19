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

package zone.dragon.dropwizard.metrics.interceptors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInterceptor;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Counted;

import lombok.NonNull;
import zone.dragon.dropwizard.AnnotatedConstructorInterceptorFactory;
import zone.dragon.dropwizard.AnnotatedMethodInterceptorFactory;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * Method interceptor that counts calls to methods annotated with {@link Counted @Counted} using a {@link Counter}
 */
@Singleton
public class CountedInterceptorFactory
    implements AnnotatedMethodInterceptorFactory<Counted>, AnnotatedConstructorInterceptorFactory<Counted> {
    private final MetricRegistry metricRegistry;

    private final MetricNameService metricNameService;

    @Inject
    public CountedInterceptorFactory(@NonNull MetricRegistry metricRegistry, @NonNull MetricNameService metricNameService) {
        this.metricRegistry = metricRegistry;
        this.metricNameService = metricNameService;
    }

    protected Object count(Executable executable, Counted annotation, Invocation invocation) throws Throwable {
        Counter counter = getCounter(executable);
        try {
            counter.inc();
            return invocation.proceed();
        } finally {
            if (!annotation.monotonic()) {
                counter.dec();
            }
        }
    }

    protected Counter getCounter(Executable executable) {
        return metricRegistry.counter(metricNameService.getFormattedMetricName(executable, Counter.class));
    }

    @Override
    public MethodInterceptor provide(Method method, Counted annotation) {
        return invocation -> count(method, annotation, invocation);
    }

    @Override
    public ConstructorInterceptor provide(Constructor<?> constructor, Counted annotation) {
        return invocation -> count(constructor, annotation, invocation);
    }
}
