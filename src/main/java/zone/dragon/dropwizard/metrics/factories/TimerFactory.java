package zone.dragon.dropwizard.metrics.factories;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.glassfish.hk2.api.InstantiationService;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory that injects tagged {@link Timer timers}
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
