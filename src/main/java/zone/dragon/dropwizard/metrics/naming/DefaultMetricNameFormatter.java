package zone.dragon.dropwizard.metrics.naming;

import com.google.common.base.Joiner;

import javax.inject.Singleton;
import java.util.TreeMap;

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
        // Tree map to ensure that tags are sorted and always appear in a deterministic order
        String tags = Joiner.on(", ").withKeyValueSeparator("=").join(new TreeMap<>(metricName.getTags()));
        return String.format("%s{%s}", metricName.getName(), tags);
    }
}
