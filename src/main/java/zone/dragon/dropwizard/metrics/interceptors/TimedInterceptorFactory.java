package zone.dragon.dropwizard.metrics.interceptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.concurrent.CompletionStage;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInterceptor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.codahale.metrics.annotation.Timed;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.HttpMethod;
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

    protected boolean isResourceMethod(Method method) {
        // Check for the HttpMethod meta-annotation
        for (Annotation ann : method.getAnnotations()) {
            if (ann.annotationType().getAnnotation(HttpMethod.class) != null) {
                return true;
            }
        }
        return false;
    }

    protected Object timeAsync(Executable executable, Invocation invocation) throws Throwable {
        Context context = getTimer(executable).time();
        try {
            CompletionStage<?> promise = (CompletionStage<?>) invocation.proceed();
            promise.whenComplete((result, error) -> context.stop());
            return promise;
        } catch (Throwable t) {
            context.stop();
            throw t;
        }
    }

    protected Object time(Executable executable, Invocation invocation) throws Throwable {
        try (Context ignored = getTimer(executable).time()) {
            return invocation.proceed();
        }
    }
}
