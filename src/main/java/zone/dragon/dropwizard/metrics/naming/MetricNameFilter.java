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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import org.glassfish.jersey.spi.Contract;

/**
 * Filter for renaming or tagging metrics
 */
@Contract
@org.jvnet.hk2.annotations.Contract
public interface MetricNameFilter {
    /**
     * Priority for filters that generate a base name for a metric when one doesn't already exist
     */
    int DEFAULT_NAME_PRIORITY = 4000;

    /**
     * Priority for filters that primarily update, replace, or otherwise alter existing metric names
     */
    int OVERRIDE_NAME_PRIORITY = 3000;

    /**
     * Priority for filters that primarily add new tags to a metric
     */
    int DEFAULT_TAG_PRIORITY = 2000;

    /**
     * Priority for filters that primarily override or update existing tags on a metric
     */
    int OVERRIDE_TAG_PRIORITY = 1000;

    /**
     * Updates the name or tags on a metric, or replaces them entirely
     *
     * @param metricName
     *     The existing name and tags for this metric
     * @param injectionSite
     *     Element that triggered creation of this metric, or {@code null} if it was manually created
     * @param metricType
     *     Type of metric being created, or {@code null} if not known
     *
     * @return The updated {@code metricName}
     */
    MetricName buildName(MetricName metricName, AnnotatedElement injectionSite, Type metricType);
}
