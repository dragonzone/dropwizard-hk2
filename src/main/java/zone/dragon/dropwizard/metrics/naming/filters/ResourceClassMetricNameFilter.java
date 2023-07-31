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

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ResourceInfo;
import lombok.NonNull;
import zone.dragon.dropwizard.metrics.naming.MetricName;
import zone.dragon.dropwizard.metrics.naming.MetricNameFilter;

import static com.google.common.base.Preconditions.checkState;

/**
 * Tags request metrics with the name of the resource class handling the request
 */
@Singleton
@Priority(MetricNameFilter.DEFAULT_TAG_PRIORITY)
public class ResourceClassMetricNameFilter extends RequestScopedMetricNameFilter {
    public static final String DEFAULT_TAG_NAME = "resource";

    private final String tagName;

    private Provider<ResourceInfo> resourceInfoProvider;

    /**
     * Creates a filter that tags request-scoped metrics with a tag named {@link #DEFAULT_TAG_NAME "resource"} and the current resource
     * class simple name as the value
     */
    @Inject
    public ResourceClassMetricNameFilter() {
        this(DEFAULT_TAG_NAME);
    }

    /**
     * Creates a filter that tags request-scoped metrics with a tag named {@code tagName} and the current resource class simple name as the
     * value
     *
     * @param tagName
     *     Name to use for the tag
     */
    public ResourceClassMetricNameFilter(@NonNull String tagName) {
        this.tagName = tagName;
    }

    @Override
    public MetricName buildRequestScopedName(MetricName metricName, AnnotatedElement injectionSite, Type metricType) {
        checkState(resourceInfoProvider != null, "resourceInfo must be set");
        ResourceInfo resourceInfo = resourceInfoProvider.get();
        if (resourceInfo == null || resourceInfo.getResourceClass() == null) {
            return metricName;
        }
        return metricName.addTag(tagName, resourceInfo.getResourceClass().getSimpleName());
    }

    public void setResourceInfoProvider(@NonNull Provider<ResourceInfo> resourceInfoProvider) {
        this.resourceInfoProvider = resourceInfoProvider;
    }
}

