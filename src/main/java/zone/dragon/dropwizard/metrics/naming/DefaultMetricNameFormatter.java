package zone.dragon.dropwizard.metrics.naming;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import javax.inject.Singleton;
import java.util.TreeMap;

/**
 * Formats metric names as a name followed by a set of tags
 */
@Singleton
public class DefaultMetricNameFormatter implements MetricNameFormatter {
    private final String tagSeparator;
    private final String kvSeparator;
    private final String nameFormat;
    private final String taggedNameFormat;

    /**
     * Formats metric names as a name followed by a comma-separated tags inside of <code>{}</code>
     */
    public DefaultMetricNameFormatter() {
        this(null, null, "{", null, "=", ", ", null, "}");
    }

    public DefaultMetricNameFormatter(
        String namePrefix,
        String nameSuffix,
        String tagsPrefix,
        String tagPrefix,
        String kvSeparator,
        String tagSeparator,
        String tagSuffix,
        String tagsSuffix
    ) {
        this.tagSeparator = Strings.nullToEmpty(tagSuffix) + Strings.nullToEmpty(tagSeparator) + Strings.nullToEmpty(tagPrefix);
        this.kvSeparator = kvSeparator;
        StringBuilder sb = new StringBuilder();
        sb.append(Strings.nullToEmpty(namePrefix)).append("%s").append(Strings.nullToEmpty(nameSuffix));
        nameFormat = sb.toString();
        sb
            .append(Strings.nullToEmpty(tagsPrefix))
            .append(Strings.nullToEmpty(tagPrefix))
            .append("%s")
            .append(Strings.nullToEmpty(tagSuffix))
            .append(Strings.nullToEmpty(tagsSuffix));
        taggedNameFormat = sb.toString();
    }

    @Override
    public String formatName(MetricName metricName) {
        if (metricName.getTags().isEmpty()) {
            return String.format(nameFormat, metricName.getName());
        }
        // Tree map to ensure that tags are sorted and always appear in a deterministic order
        String tags = Joiner.on(tagSeparator).withKeyValueSeparator(kvSeparator).join(new TreeMap<>(metricName.getTags()));
        return String.format(taggedNameFormat, metricName.getName(), tags);
    }
}
