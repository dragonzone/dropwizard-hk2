package zone.dragon.dropwizard.metrics.factories;

import org.glassfish.hk2.api.InstantiationService;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * Factory that injects tagged {@link Timer timers}
 *
 * @see MetricRegistry
 * @see MetricNameService
 */
@Singleton
public class TimerFactory extends MetricFactory<Timer> {
    @Inject
    public TimerFactory(InstantiationService instantiationService, MetricNameService nameService, MetricRegistry metricRegistry) {
        super(instantiationService, nameService, metricRegistry::timer);
    }
}
