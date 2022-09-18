package zone.dragon.dropwizard.metrics.factories;

import org.glassfish.hk2.api.InstantiationService;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * Factory that injects tagged {@link Meter meters}
 *
 * @see MetricRegistry
 * @see MetricNameService
 */
@Singleton
public class MeterFactory extends MetricFactory<Meter> {
    @Inject
    public MeterFactory(InstantiationService instantiationService, MetricNameService nameService, MetricRegistry metricRegistry) {
        super(instantiationService, nameService, metricRegistry::meter);
    }
}
