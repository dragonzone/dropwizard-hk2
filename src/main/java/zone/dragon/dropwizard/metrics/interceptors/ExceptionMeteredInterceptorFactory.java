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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInterceptor;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;

import lombok.NonNull;
import zone.dragon.dropwizard.AnnotatedConstructorInterceptorFactory;
import zone.dragon.dropwizard.AnnotatedMethodInterceptorFactory;
import zone.dragon.dropwizard.AnnotationInterceptionService;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * Interceptor that counts exceptions thrown from methods and constructors annotated with {@link ExceptionMetered @ExceptionMetered} using a
 * {@link Meter}
 *
 * @see AnnotationInterceptionService
 */
@Singleton
public class ExceptionMeteredInterceptorFactory
    implements AnnotatedMethodInterceptorFactory<ExceptionMetered>, AnnotatedConstructorInterceptorFactory<ExceptionMetered> {
    private final MetricRegistry metricRegistry;

    private final MetricNameService metricNameService;

    /**
     * @param metricRegistry
     *     Registry used for creating metrics
     * @param metricNameService
     *     Naming service used to build contextual metric names
     */
    @Inject
    public ExceptionMeteredInterceptorFactory(@NonNull MetricRegistry metricRegistry, @NonNull MetricNameService metricNameService) {
        this.metricRegistry = metricRegistry;
        this.metricNameService = metricNameService;
    }

    /**
     * Runs the {@code invocation} and tracks when exceptions are thrown
     *
     * @param executable
     *     Executable represented by {@code invocation}; used to determine the name of the
     * @param annotation
     *     Annotation instance containing information on which exceptions are metered
     * @param invocation
     *     Intercepted execution that should be metered
     *
     * @return The result of {@code invocation}
     *
     * @throws Throwable
     *     Any exception thrown by {@code invocation}
     */
    protected Object exceptionMeter(Executable executable, ExceptionMetered annotation, Invocation invocation) throws Throwable {
        Meter exceptionMeter = getExceptionMeter(executable);
        try {
            return invocation.proceed();
        } catch (Throwable t) {
            if (annotation.cause().isAssignableFrom(t.getClass())) {
                exceptionMeter.mark();
            }
            throw t;
        }
    }

    /**
     * Runs the {@code invocation} and tracks when exceptions are thrown either by the method or a {@link CompletionStage} returned by the
     * method
     *
     * @param executable
     *     Executable represented by {@code invocation}; used to determine the name of the
     * @param annotation
     *     Annotation instance containing information on which exceptions are metered
     * @param invocation
     *     Intercepted execution that should be metered
     *
     * @return The result of {@code invocation}
     *
     * @throws Throwable
     *     Any exception thrown by {@code invocation}
     */
    protected Object exceptionMeterAsync(Executable executable, ExceptionMetered annotation, Invocation invocation) throws Throwable {
        Meter exceptionMeter = getExceptionMeter(executable);
        try {
            CompletionStage<?> promise = (CompletionStage<?>) invocation.proceed();
            if (promise != null) {
                promise.whenComplete((result, error) -> {
                    if (annotation.cause().isAssignableFrom(error.getClass())) {
                        exceptionMeter.mark();
                    }
                });
            }
            return promise;
        } catch (Throwable t) {
            if (annotation.cause().isAssignableFrom(t.getClass())) {
                exceptionMeter.mark();
            }
            throw t;
        }
    }

    /**
     * Creates a {@link Meter} for the given {@code executable}
     *
     * @param executable
     *     The method or constructor to track
     *
     * @return An appropriately named meter
     */
    protected Meter getExceptionMeter(Executable executable) {
        return metricRegistry.meter(metricNameService.getFormattedMetricName(executable, Meter.class));
    }

    @Override
    public MethodInterceptor provide(Method method, ExceptionMetered annotation) {
        // Skip resource methods
        if (isResourceMethod(method)) {
            return null;
        }
        if (CompletionStage.class.isAssignableFrom(method.getReturnType())) {
            return invocation -> exceptionMeterAsync(method, annotation, invocation);
        }
        return invocation -> exceptionMeter(method, annotation, invocation);
    }

    @Override
    public ConstructorInterceptor provide(Constructor<?> constructor, ExceptionMetered annotation) {
        return invocation -> exceptionMeter(constructor, annotation, invocation);
    }

    protected boolean isResourceMethod(Method method) {
        // Check for the HttpMethod meta-annotation
        for (Annotation ann : method.getAnnotations()) {
            if (ann.annotationType().getAnnotation(HttpMethod.class) != null) {
                return true;
            }
        }
        return false;
    }
}
