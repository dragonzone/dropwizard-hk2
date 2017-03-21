package zone.dragon.dropwizard.metrics.naming;

import com.google.common.base.Joiner;

import javax.inject.Singleton;

/**
 * Formats metric names as a name followed by a comma-separated tags inside of <code>{}</code>
 */
@Singleton
public class DefaultMetricNameFormatter implements MetricNameFormatter {
    @Override
    public String formatName(MetricName metricName) {
        if (metricName.getTags().isEmpty()) {
            return metricName.getName();
        }
        String tags = Joiner.on(", ").withKeyValueSeparator("=").join(metricName.getTags());
        return String.format("%s{%s}", metricName.getName(), tags);
    }
}
