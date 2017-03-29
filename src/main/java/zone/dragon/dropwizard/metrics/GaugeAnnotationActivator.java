package zone.dragon.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Gauge;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.api.ActiveDescriptor;
import zone.dragon.dropwizard.MethodAnnotationActivator;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * When a (@link Singleton singleton} is activated, any parameterless methods annotated with {@link Gauge @Gauge} will be
 * used to provide values for a {@link com.codahale.metrics.Gauge Gauge}
 */
@Slf4j
@Singleton
public class GaugeAnnotationActivator extends MethodAnnotationActivator<Gauge> {
    private final MetricRegistry    metricRegistry;
    private final MetricNameService metricNameService;

    @Inject
    public GaugeAnnotationActivator(@NonNull MetricRegistry metricRegistry, @NonNull MetricNameService metricNameService) {
        super(Gauge.class);
        this.metricRegistry = metricRegistry;
        this.metricNameService = metricNameService;
    }

    @Override
    protected void activate(ActiveDescriptor<?> handle, Object service, Method method, Gauge annotation) {
        if (method.getParameterCount() != 0) {
            log.error("@Gauge placed on method {} which must have zero parameters, but has {}", method, method.getParameterCount());
            return;
        }
        com.codahale.metrics.Gauge<?> gauge = () -> {
            try {
                return method.invoke(service);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read gauge value from " + method, e instanceof InvocationTargetException ? (
                    (InvocationTargetException) e
                ).getTargetException() : e);
            }
        };
        String name = metricNameService.getFormattedMetricName(method, com.codahale.metrics.Gauge.class);
        log.debug("Activating gauge {} monitoring {}", name, method);
        metricRegistry.register(name, gauge);
    }
}
