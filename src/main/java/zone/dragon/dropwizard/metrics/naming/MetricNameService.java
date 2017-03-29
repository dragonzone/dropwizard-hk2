package zone.dragon.dropwizard.metrics.naming;

import org.jvnet.hk2.annotations.Contract;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

/**
 * Service that generates a metric name for injection sites based on the context of the injection site and any annotations on the
 * injection site.
 */
@Contract
@org.glassfish.jersey.spi.Contract
public interface MetricNameService {
    /**
     * Returns a contextual name for a metric as a formatted string, given the injection site and type of metric; this may include
     * additional information from the current application scope. A default base name is assumed
     *
     * @param injectionSite
     *     Parameter/field being injected or method/constructor being intercepted, or {@code null} if a metric is not being named due to an
     *     injection.
     * @param metricType
     *     Type of metric that is being named
     *
     * @return Contextual name for the metric
     */
    default String getFormattedMetricName(AnnotatedElement injectionSite, Type metricType) {
        return getFormattedMetricName(injectionSite, metricType, null);
    }

    /**
     * Returns a contextual name for a metric, given the injection site and type of metric; this may include additional information from
     * the current application scope.
     *
     * @param injectionSite
     *     Parameter/field being injected or method/constructor being intercepted, or {@code null} if a metric is not being named due to an
     *     injection.
     * @param metricType
     *     Type of metric that is being named
     * @param baseName
     *     Original metric name, as suggested by caller; This may be modified by name filters
     *
     * @return Contextual name for the metric
     */

    String getFormattedMetricName(AnnotatedElement injectionSite, Type metricType, String baseName);

    /**
     * Returns a contextual name for a metric, given the injection site and type of metric; this may include additional information from
     * the current application scope. A default base name is assumed
     *
     * @param injectionSite
     *     Parameter/field being injected or method/constructor being intercepted, or {@code null} if a metric is not being named due to an
     *     injection.
     * @param metricType
     *     Type of metric that is being named
     *
     * @return Contextual name for the metric
     */
    default MetricName getMetricName(AnnotatedElement injectionSite, Type metricType) {
        return getMetricName(injectionSite, metricType, null);
    }

    /**
     * Returns a contextual name for a metric, given the injection site and type of metric; this may include additional information from
     * the current application scope.
     *
     * @param injectionSite
     *     Parameter/field being injected or method/constructor being intercepted, or {@code null} if a metric is not being named due to an
     *     injection.
     * @param metricType
     *     Type of metric that is being named
     * @param baseName
     *     Original metric name, as suggested by caller; This may be modified by name filters
     *
     * @return Contextual name for the metric
     */
    MetricName getMetricName(AnnotatedElement injectionSite, Type metricType, String baseName);

}
