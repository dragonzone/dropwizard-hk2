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
import com.codahale.metrics.annotation.ExceptionMetered;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.HttpMethod;
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
    private final MetricRegistry    metricRegistry;
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
