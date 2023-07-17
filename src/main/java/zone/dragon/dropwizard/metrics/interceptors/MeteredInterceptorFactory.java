package zone.dragon.dropwizard.metrics.interceptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInterceptor;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metered;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.HttpMethod;
import lombok.NonNull;
import zone.dragon.dropwizard.AnnotatedConstructorInterceptorFactory;
import zone.dragon.dropwizard.AnnotatedMethodInterceptorFactory;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * Method interceptor that counts calls to methods annotated with {@link Metered @Metered} using a {@link Meter}
 */
@Singleton
public class MeteredInterceptorFactory
    implements AnnotatedMethodInterceptorFactory<Metered>, AnnotatedConstructorInterceptorFactory<Metered> {
    private final MetricRegistry metricRegistry;

    private final MetricNameService metricNameService;

    @Inject
    public MeteredInterceptorFactory(@NonNull MetricRegistry metricRegistry, @NonNull MetricNameService metricNameService) {
        this.metricRegistry = metricRegistry;
        this.metricNameService = metricNameService;
    }

    protected Meter getMeter(Executable executable) {
        return metricRegistry.meter(metricNameService.getFormattedMetricName(executable, Meter.class));
    }

    protected Object meter(Executable executable, Invocation invocation) throws Throwable {
        getMeter(executable).mark();
        return invocation.proceed();
    }

    @Override
    public ConstructorInterceptor provide(Constructor<?> constructor, Metered annotation) {
        return invocation -> meter(constructor, invocation);
    }

    @Override
    public MethodInterceptor provide(Method method, Metered annotation) {
        // Skip resource methods
        for (Annotation ann : method.getAnnotations()) {
            if (ann.annotationType().getAnnotation(HttpMethod.class) != null) {
                return null;
            }
        }
        return invocation -> meter(method, invocation);
    }
}
