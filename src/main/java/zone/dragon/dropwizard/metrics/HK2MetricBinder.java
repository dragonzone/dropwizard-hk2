package zone.dragon.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import zone.dragon.dropwizard.metrics.naming.CodahaleMetricNameFilter;
import zone.dragon.dropwizard.metrics.naming.DefaultMetricNameFormatter;
import zone.dragon.dropwizard.metrics.naming.DefaultMetricNameService;
import zone.dragon.dropwizard.metrics.naming.MetricNameFilter;
import zone.dragon.dropwizard.metrics.naming.MetricNameFormatter;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

import javax.inject.Singleton;

/**
 * Binder that registers all of the metric components with HK2; It is expected that the {@link MetricRegistry} is already bound in HK2.
 */
public class HK2MetricBinder extends AbstractBinder {
    @Override
    protected void configure() {
        // Binding naming services
        bind(DefaultMetricNameService.class).to(MetricNameService.class).in(Singleton.class);
        bind(DefaultMetricNameFormatter.class).to(MetricNameFormatter.class).in(Singleton.class);
        bind(CodahaleMetricNameFilter.class).to(MetricNameFilter.class).in(Singleton.class).ranked(MetricNameFilter.DEFAULT_NAME_PRIORITY);
        bindAsContract(TaggedMetricRegistry.class).in(Singleton.class);
        // Bind Metric method handlers
        bind(GaugeAnnotationActivator.class).to(InstanceLifecycleListener.class).in(Singleton.class);
        bind(CachedGaugeAnnotationActivator.class).to(InstanceLifecycleListener.class).in(Singleton.class);
    }
}
