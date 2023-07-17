package zone.dragon.dropwizard.metrics.naming;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import org.glassfish.hk2.api.IterableProvider;
import org.jvnet.hk2.annotations.Optional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;

/**
 * Builds metric names using all available {@link MetricNameFilter} and a {@link MetricNameFormatter}; Filters are processed descending rank
 * order, with highest rank running first and lowest rank running last.
 */
@Singleton
public class DefaultMetricNameService implements MetricNameService {
    private final IterableProvider<MetricNameFilter> filters;

    private final MetricNameFormatter formatter;

    @Inject
    public DefaultMetricNameService(@NonNull IterableProvider<MetricNameFilter> filters, @Optional MetricNameFormatter formatter) {
        this.filters = filters;
        this.formatter = formatter == null ? new DefaultMetricNameFormatter() : formatter;
    }

    @Override
    public MetricName getMetricName(AnnotatedElement injectionSite, Type metricType, String baseName) {
        MetricName name = MetricName.of(baseName);
        for (MetricNameFilter filter : filters) {
            name = filter.buildName(name, injectionSite, metricType);
        }
        return name;
    }

    @Override
    public String getFormattedMetricName(AnnotatedElement injectionSite, Type metricType, String baseName) {
        return formatter.formatName(getMetricName(injectionSite, metricType, baseName));
    }
}
