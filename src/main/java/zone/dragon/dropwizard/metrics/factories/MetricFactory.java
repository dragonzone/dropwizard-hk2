package zone.dragon.dropwizard.metrics.factories;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Function;

import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InstantiationService;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.Visibility;

import com.codahale.metrics.Metric;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * Factory that handles injecting metrics named based on the injection site
 */
@Singleton
@Slf4j
public class MetricFactory<T extends Metric> implements Factory<T> {
    private final InstantiationService instantiationService;
    private final MetricNameService    nameService;
    private final Function<String, T>  metricSupplier;

    @Inject
    public MetricFactory(
        @NonNull InstantiationService instantiationService,
        @NonNull MetricNameService metricNameService,
        @NonNull Function<String, T> metricSupplier
    ) {
        this.instantiationService = instantiationService;
        this.nameService = metricNameService;
        this.metricSupplier = metricSupplier;
    }

    @Override
    @PerLookup
    @Visibility(DescriptorVisibility.LOCAL)
    public T provide() {
        Injectee injectee = instantiationService.getInstantiationData().getParentInjectee();
        String   name;
        if (injectee == null) {
            log.warn("Creating metric with no injection context; Use the metric registry directly instead of dynamically creating it "
                     + "through HK2", new Exception());
            name = UUID.randomUUID().toString();
        } else {
            AnnotatedElement parent = injectee.getParent();
            if (parent instanceof Constructor) {
                parent = ((Constructor) parent).getParameters()[injectee.getPosition()];
            }
            if (parent instanceof Method) {
                parent = ((Method) parent).getParameters()[injectee.getPosition()];
            }
            name = nameService.getFormattedMetricName(parent, injectee.getInjecteeClass());
        }
        return metricSupplier.apply(name);
    }

    @Override
    public void dispose(T instance) {
        //does nothing
    }
}
