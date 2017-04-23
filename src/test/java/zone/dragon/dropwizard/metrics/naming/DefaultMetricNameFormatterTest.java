package zone.dragon.dropwizard.metrics.naming;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultMetricNameFormatterTest {
    private final DefaultMetricNameFormatter formatter = new DefaultMetricNameFormatter("NP", "NS", "TSP", "TP", "KV", "TE", "TX", "TSS");

    @Test
    public void testNoTagsName() {
        MetricName name          = MetricName.of("test");
        String     formattedName = formatter.formatName(name);
        assertThat(formattedName).isEqualTo("NPtestNS");
    }

    @Test(expected = NullPointerException.class)
    public void testNullMetricName() {
        formatter.formatName(null);
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
