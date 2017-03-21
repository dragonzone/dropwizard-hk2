package zone.dragon.dropwizard.metrics.naming;

import org.jvnet.hk2.annotations.Contract;

/**
 * Formatter that serializes a metric name to a string
 */
@Contract
@org.glassfish.jersey.spi.Contract
public interface MetricNameFormatter {
    String formatName(MetricName metricName);

}
