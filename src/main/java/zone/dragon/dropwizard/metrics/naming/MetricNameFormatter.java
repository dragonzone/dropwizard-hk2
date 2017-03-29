package zone.dragon.dropwizard.metrics.naming;

import org.jvnet.hk2.annotations.Contract;

/**
 * Formatter that serializes a metric name to a string
 */
@Contract
@org.glassfish.jersey.spi.Contract
public interface MetricNameFormatter {
    /**
     * Formats a {@link MetricName} into a string, encoding tags in a format understood by whatever clients are accessing the metric
     * registry
     *
     * @param metricName
     *     The metric name to serialize
     *
     * @return The serialized form of the metric name
     */
    String formatName(MetricName metricName);

}
