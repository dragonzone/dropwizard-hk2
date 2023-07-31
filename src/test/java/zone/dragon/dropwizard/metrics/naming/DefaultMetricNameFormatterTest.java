/*
 * MIT License
 *
 * Copyright (c) 2016-2023 Bryan Harclerode
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package zone.dragon.dropwizard.metrics.naming;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DefaultMetricNameFormatterTest {
    private final DefaultMetricNameFormatter formatter = new DefaultMetricNameFormatter("NP", "NS", "TSP", "TP", "KV", "TE", "TX", "TSS");

    @Test
    public void testNoTagsName() {
        MetricName name = MetricName.of("test");
        String formattedName = formatter.formatName(name);
        assertThat(formattedName).isEqualTo("NPtestNS");
    }

    @Test
    public void testNullMetricName() {
        assertThatThrownBy(() -> formatter.formatName(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testTaggedNameMulti() {
        MetricName name = MetricName.of("test").addTag("key", "value").addTag("key2", "value2");
        String formattedName = formatter.formatName(name);
        assertThat(formattedName).isEqualTo("NPtestNSTSPTPkeyKVvalueTXTETPkey2KVvalue2TXTSS");
    }

    @Test
    public void testTaggedNameSingle() {
        MetricName name = MetricName.of("test").addTag("key", "value");
        String formattedName = formatter.formatName(name);
        assertThat(formattedName).isEqualTo("NPtestNSTSPTPkeyKVvalueTXTSS");
    }
}
