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
