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

import org.jvnet.hk2.annotations.Contract;

/**
 * Service that generates a metric name for injection sites based on the context of the injection site and any annotations on the injection
 * site.
 */
@Contract
@org.glassfish.jersey.spi.Contract
public interface MetricNameService {
    /**
     * Returns a contextual name for a metric as a formatted string, given the injection site and type of metric; this may include
     * additional information from the current application scope. A default base name is assumed
     *
     * @param injectionSite
     *     Parameter/field being injected or method/constructor being intercepted, or {@code null} if a metric is not being named due to an
     *     injection.
     * @param metricType
     *     Type of metric that is being named
     *
     * @return Contextual name for the metric
     */
    default String getFormattedMetricName(AnnotatedElement injectionSite, Type metricType) {
        return getFormattedMetricName(injectionSite, metricType, null);
    }

    /**
     * Returns a contextual name for a metric, given the injection site and type of metric; this may include additional information from the
     * current application scope.
     *
     * @param injectionSite
     *     Parameter/field being injected or method/constructor being intercepted, or {@code null} if a metric is not being named due to an
     *     injection.
     * @param metricType
     *     Type of metric that is being named
     * @param baseName
     *     Original metric name, as suggested by caller; This may be modified by name filters
     *
     * @return Contextual name for the metric
     */

    String getFormattedMetricName(AnnotatedElement injectionSite, Type metricType, String baseName);

    /**
     * Returns a contextual name for a metric, given the injection site and type of metric; this may include additional information from the
     * current application scope. A default base name is assumed
     *
     * @param injectionSite
     *     Parameter/field being injected or method/constructor being intercepted, or {@code null} if a metric is not being named due to an
     *     injection.
     * @param metricType
     *     Type of metric that is being named
     *
     * @return Contextual name for the metric
     */
    default MetricName getMetricName(AnnotatedElement injectionSite, Type metricType) {
        return getMetricName(injectionSite, metricType, null);
    }

    /**
     * Returns a contextual name for a metric, given the injection site and type of metric; this may include additional information from the
     * current application scope.
     *
     * @param injectionSite
     *     Parameter/field being injected or method/constructor being intercepted, or {@code null} if a metric is not being named due to an
     *     injection.
     * @param metricType
     *     Type of metric that is being named
     * @param baseName
     *     Original metric name, as suggested by caller; This may be modified by name filters
     *
     * @return Contextual name for the metric
     */
    MetricName getMetricName(AnnotatedElement injectionSite, Type metricType, String baseName);

}
