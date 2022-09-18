package zone.dragon.dropwizard.metrics.factories;

import org.glassfish.hk2.api.InstantiationService;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * Factory that injects tagged {@link Counter counters}
 *
 * @see MetricRegistry
 * @see MetricNameService
 */
@Singleton
public class CounterFactory extends MetricFactory<Counter> {
    @Inject
    public CounterFactory(InstantiationService instantiationService, MetricNameService nameService, MetricRegistry metricRegistry) {
        super(instantiationService, nameService, metricRegistry::counter);
    }
}
