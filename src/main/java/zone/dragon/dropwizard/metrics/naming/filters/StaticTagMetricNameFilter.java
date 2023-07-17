package zone.dragon.dropwizard.metrics.naming.filters;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import zone.dragon.dropwizard.metrics.naming.MetricName;
import zone.dragon.dropwizard.metrics.naming.MetricNameFilter;

/**
 * Applies a static tag to all metrics
 */
@Singleton
@Priority(MetricNameFilter.DEFAULT_TAG_PRIORITY)
@RequiredArgsConstructor
public class StaticTagMetricNameFilter implements MetricNameFilter {
    /**
     * Name of the tag to add
     */
    @NonNull
    private final String tagName;

    /**
     * Value of the tag to add
     */
    @NonNull
    private final String tagValue;

    @Override
    public MetricName buildName(MetricName metricName, AnnotatedElement injectionSite, Type metricType) {
        return metricName.addTag(tagName, tagValue);
    }
}
