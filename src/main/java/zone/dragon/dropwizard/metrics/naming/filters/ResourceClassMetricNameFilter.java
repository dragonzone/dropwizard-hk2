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
 * Tags request metrics with the name of the resource class handling the request
 */
@Singleton
@Priority(MetricNameFilter.DEFAULT_TAG_PRIORITY)
public class ResourceClassMetricNameFilter extends RequestScopedMetricNameFilter {
    public static final String DEFAULT_TAG_NAME = "resource";
    private final String                 tagName;
    private       Provider<ResourceInfo> resourceInfoProvider;

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

