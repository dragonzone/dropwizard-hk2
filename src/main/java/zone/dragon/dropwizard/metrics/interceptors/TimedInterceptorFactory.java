package zone.dragon.dropwizard.metrics.interceptors;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.codahale.metrics.annotation.Timed;
import lombok.NonNull;
import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInterceptor;
import zone.dragon.dropwizard.AnnotatedConstructorInterceptorFactory;
import zone.dragon.dropwizard.AnnotatedMethodInterceptorFactory;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * Method interceptor that times methods annotated with {@link Timed @Timed} using a {@link Timer}
 */
@Singleton
public class TimedInterceptorFactory implements AnnotatedMethodInterceptorFactory<Timed>, AnnotatedConstructorInterceptorFactory<Timed> {
    private final MetricRegistry    metricRegistry;
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
        // Skip resource methods
        for (Annotation ann : method.getAnnotations()) {
            if (ann.annotationType().getAnnotation(HttpMethod.class) != null) {
                return null;
            }
        }
        return invocation -> time(method, invocation);
    }

    @Override
    public ConstructorInterceptor provide(Constructor<?> constructor, Timed annotation) {
        return invocation -> time(constructor, invocation);
    }

    protected Object time(Executable executable, Invocation invocation) throws Throwable {
        Context context = getTimer(executable).time();
        try {
            return invocation.proceed();
        } finally {
            context.stop();
        }
    }
}
