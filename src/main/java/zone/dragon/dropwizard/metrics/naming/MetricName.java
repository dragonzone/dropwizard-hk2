package zone.dragon.dropwizard.metrics.naming;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.Wither;

/**
 */
@Value(staticConstructor = "of")
@Builder(toBuilder = true)
public class MetricName {
    @NonNull
    @Wither
    private final String                       name;
    @Singular
    @Wither
    @NonNull
    private       ImmutableMap<String, String> tags;

    public MetricName withTag(String key, String value) {
        return toBuilder().tag(key, value).build();
    }
}
