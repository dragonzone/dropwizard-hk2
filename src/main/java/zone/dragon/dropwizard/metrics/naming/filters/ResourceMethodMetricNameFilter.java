package zone.dragon.dropwizard.metrics.naming.filters;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.container.ResourceInfo;

import lombok.NonNull;
import zone.dragon.dropwizard.metrics.naming.MetricName;
import zone.dragon.dropwizard.metrics.naming.MetricNameFilter;

import static com.google.common.base.Preconditions.checkState;

/**
 * Tags request metrics with the name of the resource method handling the request
 */
@Singleton
@Priority(MetricNameFilter.DEFAULT_TAG_PRIORITY)
public class ResourceMethodMetricNameFilter extends RequestScopedMetricNameFilter {
    public static final String DEFAULT_TAG_NAME = "action";
    private final String                 tagName;
    private       Provider<ResourceInfo> resourceInfoProvider;

    /**
     * Creates a filter that tags request-scoped metrics with a tag named {@link #DEFAULT_TAG_NAME "action"} and the current resource
     * method name as the value
     */
    @Inject
    public ResourceMethodMetricNameFilter() {
        this(DEFAULT_TAG_NAME);
    }

    /**
     * Creates a filter that tags request-scoped metrics with a tag named {@code tagName} and the current resource method name as the value
     *
     * @param tagName
     *     Name to use for the tag
     */
    public ResourceMethodMetricNameFilter(@NonNull String tagName) {
        this.tagName = tagName;
    }

    @Override
    public MetricName buildRequestScopedName(MetricName metricName, AnnotatedElement injectionSite, Type metricType) {
        checkState(resourceInfoProvider != null, "resourceInfo must be set");
        ResourceInfo resourceInfo = resourceInfoProvider.get();
        if (resourceInfo == null || resourceInfo.getResourceMethod() == null) {
            return metricName;
        }
        return metricName.addTag(tagName, resourceInfo.getResourceMethod().getName());
    }

    public void setResourceInfoProvider(@NonNull Provider<ResourceInfo> resourceInfoProvider) {
        this.resourceInfoProvider = resourceInfoProvider;
    }
}

