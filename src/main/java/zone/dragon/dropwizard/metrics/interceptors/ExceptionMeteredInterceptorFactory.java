package zone.dragon.dropwizard.metrics.interceptors;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;
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
 * Method interceptor that counts exceptions thrown from methods annotated with {@link ExceptionMetered @ExceptionMetered} using a
 * {@link Meter}
 */
@Singleton
public class ExceptionMeteredInterceptorFactory
    implements AnnotatedMethodInterceptorFactory<ExceptionMetered>, AnnotatedConstructorInterceptorFactory<ExceptionMetered> {
    private final MetricRegistry    metricRegistry;
    private final MetricNameService metricNameService;

    @Inject
    public ExceptionMeteredInterceptorFactory(@NonNull MetricRegistry metricRegistry, @NonNull MetricNameService metricNameService) {
        this.metricRegistry = metricRegistry;
        this.metricNameService = metricNameService;
    }

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

    protected Meter getExceptionMeter(Executable executable) {
        return metricRegistry.meter(metricNameService.getFormattedMetricName(executable, Meter.class));
    }

    @Override
    public MethodInterceptor provide(Method method, ExceptionMetered annotation) {
        // Skip resource methods
        for (Annotation ann : method.getAnnotations()) {
            if (ann.annotationType().getAnnotation(HttpMethod.class) != null) {
                return null;
            }
        }
        return invocation -> exceptionMeter(method, annotation, invocation);
    }

    @Override
    public ConstructorInterceptor provide(Constructor<?> constructor, ExceptionMetered annotation) {
        return invocation -> exceptionMeter(constructor, annotation, invocation);
    }
}
