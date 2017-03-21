package zone.dragon.dropwizard.metrics.naming;

import org.glassfish.hk2.api.IterableProvider;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

/**
 * Builds metric names using all available {@link MetricNameFilter} and a {@link MetricNameFormatter}; Filters are processed descending
 * rank order, with highest rank running first and lowest rank running last.
 */
@Singleton
public class DefaultMetricNameService implements MetricNameService {
    private final IterableProvider<MetricNameFilter> filters;
    private final MetricNameFormatter formatter;

    @Inject
    public DefaultMetricNameService(@Nonnull IterableProvider<MetricNameFilter> filters, @Nonnull MetricNameFormatter formatter) {
        this.filters = filters;
        this.formatter = formatter;
    }

    @Override
    public MetricName getMetricName(AnnotatedElement parent, Type metricType) {
        MetricName name = MetricName.builder().name("").build();
        for (MetricNameFilter filter : filters) {
            name = filter.buildName(name, parent, metricType);
        }
        return name;
    }

    @Override
    public String getFormattedMetricName(AnnotatedElement parent, Type metricType) {
        return formatter.formatName(getMetricName(parent, metricType));
    }
}
