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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.HttpMethod;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInterceptor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.codahale.metrics.annotation.Timed;

import lombok.NonNull;
import zone.dragon.dropwizard.AnnotatedConstructorInterceptorFactory;
import zone.dragon.dropwizard.AnnotatedMethodInterceptorFactory;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * Method interceptor that times methods annotated with {@link Timed @Timed} using a {@link Timer}
 */
@Singleton
public class TimedInterceptorFactory implements AnnotatedMethodInterceptorFactory<Timed>, AnnotatedConstructorInterceptorFactory<Timed> {
    private final MetricRegistry metricRegistry;

    private final MetricNameService metricNameService;

    @Inject
    public TimedInterceptorFactory(@NonNull MetricRegistry metricRegistry, @NonNull MetricNameService metricNameService) {
        this.metricRegistry = metricRegistry;
        this.metricNameService = metricNameService;
    }

    /**
     * Retrieves or creates the {@link Timer} associated with the given executable
     *
     * The default implementation uses the {@link MetricNameService} to generate the metric name and retrieves the timer
     * from the {@link MetricRegistry}.
     *
     * @param executable the method or constructor for which to retrieve the timer
     *
     * @return the timer registered for the given executable
     */
    protected Timer getTimer(Executable executable) {
        return metricRegistry.timer(metricNameService.getFormattedMetricName(executable, Timer.class));
    }

    @Override
    public MethodInterceptor provide(Method method, Timed annotation) {
        // Skip resource methods since Dropwizard already installs a RequestEventListener for those
        if (isResourceMethod(method)) {
            return null;
        }
        if (CompletionStage.class.isAssignableFrom(method.getReturnType())) {
            return invocation -> timeAsync(method, invocation);
        }
        return invocation -> time(method, invocation);
    }

    @Override
    public ConstructorInterceptor provide(Constructor<?> constructor, Timed annotation) {
        return invocation -> time(constructor, invocation);
    }

    /**
     * Determines whether the given method is a JAX-RS resource method by checking for annotations
     * that are themselves annotated with {@link HttpMethod @HttpMethod}.
     *
     * @param method the method to check
     *
     * @return {@code true} if the method is a JAX-RS resource method, {@code false} otherwise
     */
    protected boolean isResourceMethod(Method method) {
        // Check for the HttpMethod meta-annotation
        for (Annotation ann : method.getAnnotations()) {
            if (ann.annotationType().getAnnotation(HttpMethod.class) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Times an asynchronous invocation by starting a {@link Timer.Context} and stopping it when the
     * returned {@link CompletionStage} completes. If the invocation throws synchronously, the timer
     * is stopped immediately.
     *
     * @param executable the method or constructor being timed
     * @param invocation the intercepted invocation to proceed with
     *
     * @return the {@link CompletionStage} returned by the invocation
     *
     * @throws Throwable if the invocation throws synchronously
     */
    protected Object timeAsync(Executable executable, Invocation invocation) throws Throwable {
        Context context = getTimer(executable).time();
        try {
            CompletionStage<?> promise = (CompletionStage<?>) invocation.proceed();
            if (promise != null) {
                promise.whenComplete((result, error) -> context.stop());
            }
            return promise;
        } catch (Throwable t) {
            context.stop();
            throw t;
        }
    }

    /**
     * Times a synchronous invocation by wrapping it in a {@link Timer.Context} that is
     * automatically stopped when the invocation completes or throws.
     *
     * @param executable the method or constructor being timed
     * @param invocation the intercepted invocation to proceed with
     *
     * @return the result of the invocation
     *
     * @throws Throwable if the invocation throws
     */
    protected Object time(Executable executable, Invocation invocation) throws Throwable {
        try (Context ignored = getTimer(executable).time()) {
            return invocation.proceed();
        }
    }
}
