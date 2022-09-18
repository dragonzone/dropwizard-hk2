package zone.dragon.dropwizard.metrics.naming;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DefaultMetricNameFormatterTest {
    private final DefaultMetricNameFormatter formatter = new DefaultMetricNameFormatter("NP", "NS", "TSP", "TP", "KV", "TE", "TX", "TSS");

    @Test
    public void testNoTagsName() {
        MetricName name          = MetricName.of("test");
        String     formattedName = formatter.formatName(name);
        assertThat(formattedName).isEqualTo("NPtestNS");
    }

    @Test
    public void testNullMetricName() {
        assertThatThrownBy(() -> formatter.formatName(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testTaggedNameMulti() {
        MetricName name          = MetricName.of("test").addTag("key", "value").addTag("key2", "value2");
        String     formattedName = formatter.formatName(name);
        assertThat(formattedName).isEqualTo("NPtestNSTSPTPkeyKVvalueTXTETPkey2KVvalue2TXTSS");
    }

    @Test
    public void testTaggedNameSingle() {
        MetricName name          = MetricName.of("test").addTag("key", "value");
        String     formattedName = formatter.formatName(name);
        assertThat(formattedName).isEqualTo("NPtestNSTSPTPkeyKVvalueTXTSS");
    }
}
