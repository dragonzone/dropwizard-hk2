package zone.dragon.dropwizard.metrics.naming.filters;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Request;
import lombok.NonNull;
import zone.dragon.dropwizard.metrics.naming.MetricName;
import zone.dragon.dropwizard.metrics.naming.MetricNameFilter;

import static com.google.common.base.Preconditions.checkState;

/**
 * Tags request metrics with the HTTP method of the current request
 */
@Singleton
@Priority(MetricNameFilter.DEFAULT_TAG_PRIORITY)
public class HttpMethodMetricNameFilter extends RequestScopedMetricNameFilter {
    public static final String DEFAULT_TAG_NAME = "httpOp";

    private final String tagName;

    private Provider<Request> requestProvider;

    /**
     * Creates a filter that tags request-scoped metrics with a tag named {@link #DEFAULT_TAG_NAME "httpOp"} and the current HTTP method as
     * the value
     */
    @Inject
    public HttpMethodMetricNameFilter() {
        this(DEFAULT_TAG_NAME);
    }

    /**
     * Creates a filter that tags request-scoped metrics with a tag named {@code tagName} and the current HTTP method as the value
     *
     * @param tagName
     *     Name to use for the tag
     */
    public HttpMethodMetricNameFilter(@NonNull String tagName) {
        this.tagName = tagName;
    }

    @Override
    public MetricName buildRequestScopedName(MetricName metricName, AnnotatedElement injectionSite, Type metricType) {
        checkState(requestProvider != null, "requestProvider must be set");
        Request request = requestProvider.get();
        if (request == null) {
            return metricName;
        }
        return metricName.addTag(tagName, request.getMethod());
    }

    public void setResourceInfoProvider(@NonNull Provider<Request> requestProvider) {
        this.requestProvider = requestProvider;
    }
}

