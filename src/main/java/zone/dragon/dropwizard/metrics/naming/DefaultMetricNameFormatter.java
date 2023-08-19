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

import java.util.TreeMap;

import javax.inject.Singleton;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

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
