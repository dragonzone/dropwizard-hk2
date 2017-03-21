package zone.dragon.dropwizard.metrics.factories;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import org.glassfish.hk2.api.InstantiationService;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

import javax.inject.Inject;
import javax.inject.Singleton;

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