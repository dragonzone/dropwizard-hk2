package zone.dragon.dropwizard.metrics.factories;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.glassfish.hk2.api.InstantiationService;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

import javax.inject.Inject;
import javax.inject.Singleton;

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
