package zone.dragon.dropwizard.metrics;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.glassfish.hk2.api.ActiveDescriptor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.CachedGauge;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import zone.dragon.dropwizard.MethodAnnotationActivator;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * When a {@link Singleton singleton} is activated, any parameterless methods annotated with {@link CachedGauge @CachedGauge} will be used
 * to provide values for a {@link com.codahale.metrics.CachedGauge CachedGauge}
 */
@Slf4j
@Singleton
public class CachedGaugeAnnotationActivator extends MethodAnnotationActivator<CachedGauge> {
    private final MetricRegistry metricRegistry;

    private final MetricNameService metricNameService;

    @Inject
    public CachedGaugeAnnotationActivator(@NonNull MetricRegistry metricRegistry, @NonNull MetricNameService metricNameService) {
        super(CachedGauge.class);
        this.metricRegistry = metricRegistry;
        this.metricNameService = metricNameService;
    }

    @Override
    protected void activate(ActiveDescriptor<?> handle, Object service, Method method, CachedGauge annotation) {
        if (method.getParameterCount() != 0) {
            log.error("@CachedGauge placed on method {} which must have zero parameters, but has {}", method, method.getParameterCount());
            return;
        }
        com.codahale.metrics.CachedGauge<?> gauge = new com.codahale.metrics.CachedGauge<>(
            annotation.timeout(),
            annotation.timeoutUnit()
        ) {
            @Override
            protected Object loadValue() {
                try {
                    return method.invoke(service);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to read gauge value from " + method, e instanceof InvocationTargetException ? (
                        (InvocationTargetException) e
                    ).getTargetException() : e);
                }
            }
        };
        String name = metricNameService.getFormattedMetricName(method, com.codahale.metrics.CachedGauge.class);
        log.debug("Activating cached gauge {} monitoring {}", name, method);
        metricRegistry.register(name, gauge);
    }
}
