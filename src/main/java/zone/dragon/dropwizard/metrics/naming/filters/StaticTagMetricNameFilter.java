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

package zone.dragon.dropwizard.metrics.naming.filters;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import javax.annotation.Priority;
import javax.inject.Singleton;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import zone.dragon.dropwizard.metrics.naming.MetricName;
import zone.dragon.dropwizard.metrics.naming.MetricNameFilter;

/**
 * Applies a static tag to all metrics
 */
@Singleton
@Priority(MetricNameFilter.DEFAULT_TAG_PRIORITY)
@RequiredArgsConstructor
public class StaticTagMetricNameFilter implements MetricNameFilter {
    /**
     * Name of the tag to add
     */
    @NonNull
    private final String tagName;

    /**
     * Value of the tag to add
     */
    @NonNull
    private final String tagValue;

    @Override
    public MetricName buildName(MetricName metricName, AnnotatedElement injectionSite, Type metricType) {
        return metricName.addTag(tagName, tagValue);
    }
}
