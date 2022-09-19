package zone.dragon.dropwizard.metrics.factories;

import org.glassfish.hk2.api.InstantiationService;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

/**
 * Factory that injects tagged {@link Histogram histograms}
 *
 * @see MetricRegistry
 * @see MetricNameService
 */
@Singleton
public class HistogramFactory extends MetricFactory<Histogram> {
    @Inject
    public HistogramFactory(InstantiationService instantiationService, MetricNameService nameService, MetricRegistry metricRegistry) {
        super(instantiationService, nameService, metricRegistry::histogram);
    }
}
