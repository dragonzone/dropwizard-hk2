package zone.dragon.dropwizard.metrics.interceptors;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metered;
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
 * Method interceptor that counts calls to methods annotated with {@link Metered @Metered} using a {@link Meter}
 */
@Singleton
public class MeteredInterceptorFactory
    implements AnnotatedMethodInterceptorFactory<Metered>, AnnotatedConstructorInterceptorFactory<Metered> {
    private final MetricRegistry    metricRegistry;
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
        return invocation -> meter(method, invocation);
    }
}
