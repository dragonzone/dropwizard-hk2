package zone.dragon.dropwizard.metrics.naming;

import java.util.Map;
import java.util.TreeMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Represents a named metric, which may also contain tags
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class MetricName {
    /**
     * Creates a new metric name with no tags
     *
     * @param name
     *     Name of the metric
     *
     * @return MetricName representing a bare metric
     */
    public static MetricName of(String name) {
        return of(name, new TreeMap<>());
    }

    /**
     * Creates a new metric name
     *
     * @param name
     *     Name of the metric
     * @param tags
     *     Tags for this metric
     *
     * @return MetricName representing a metric with tags
     */
    public static MetricName of(String name, @NonNull Map<String, String> tags) {
        return new MetricName(name, tags);
    }

    /**
     * Name of the metric, which uniquely identifies the type of information represented by the metric and the source of that information
     */
    private String name;

    /**
     * Contextual tags of the metric, which hold attributes describing the code path used to reach the metric.
     */
    @NonNull
    private Map<String, String> tags;

    public MetricName addTag(@NonNull String key, @NonNull String value) {
        getTags().put(key, value);
        return this;
    }
}
