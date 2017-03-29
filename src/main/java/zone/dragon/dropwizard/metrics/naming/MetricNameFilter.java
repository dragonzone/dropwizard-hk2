package zone.dragon.dropwizard.metrics.naming;

import org.glassfish.jersey.spi.Contract;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

/**
 * Filter for renaming or tagging metrics
 */
@Contract
@org.jvnet.hk2.annotations.Contract
public interface MetricNameFilter {
    /**
     * Priority for filters that generate a base name for a metric when one doesn't already exist
     */
    int DEFAULT_NAME_PRIORITY  = 4000;
    /**
     * Priority for filters that primarily update, replace, or otherwise alter existing metric names
     */
    int OVERRIDE_NAME_PRIORITY = 3000;
    /**
     * Priority for filters that primarily add new tags to a metric
     */
    int DEFAULT_TAG_PRIORITY   = 2000;
    /**
     * Priority for filters that primarily override or update existing tags on a metric
     */
    int OVERRIDE_TAG_PRIORITY  = 1000;

    /**
     * Updates the name or tags on a metric, or replaces them entirely
     *
     * @param metricName
     *     The existing name and tags for this metric
     * @param injectionSite
     *     Element that triggered creation of this metric, or {@code null} if it was manually created
     * @param metricType
     *     Type of metric being created, or {@code null} if not known
     *
     * @return The updated {@code metricName}
     */
    MetricName buildName(MetricName metricName, AnnotatedElement injectionSite, Type metricType);
}
