package zone.dragon.dropwizard.metrics.interceptors;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Counted;
import lombok.NonNull;
import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInterceptor;
import zone.dragon.dropwizard.AnnotatedConstructorInterceptorFactory;
import zone.dragon.dropwizard.AnnotatedMethodInterceptorFactory;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * Method interceptor that counts calls to methods annotated with {@link Counted @Counted} using a {@link Counter}
 */
@Singleton
public class CountedInterceptorFactory
    implements AnnotatedMethodInterceptorFactory<Counted>, AnnotatedConstructorInterceptorFactory<Counted> {
    private final MetricRegistry    metricRegistry;
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
