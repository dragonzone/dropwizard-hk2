package zone.dragon.dropwizard.metrics.naming.filters;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InstantiationData;
import org.glassfish.hk2.api.InstantiationService;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.process.internal.RequestScope.Instance;
import org.glassfish.jersey.process.internal.RequestScoped;

import lombok.NonNull;
import zone.dragon.dropwizard.metrics.naming.MetricName;
import zone.dragon.dropwizard.metrics.naming.MetricNameFilter;

import static com.google.common.base.Preconditions.checkState;

/**
 * Filter that only runs when generating names for request-scoped metrics; Use this to avoid accessing request-scoped providers or
 * modifying metrics when outside of a request scope or the metric is being injected into a non-request-scoped object.
 */
public abstract class RequestScopedMetricNameFilter implements MetricNameFilter {
    private InstantiationService instantiationService;
    private RequestScope         requestScope;

    @Override
    public MetricName buildName(MetricName metricName, AnnotatedElement injectionSite, Type metricType) {
        checkState(instantiationService != null, "instantiationService must be set");
        checkState(requestScope != null, "requestScope must be set");
        // Ensure we're inside a request scope
        Instance currentRequestScope = requestScope.suspendCurrent();
        if (currentRequestScope == null) {
            return metricName;
        } else {
            currentRequestScope.release();
        }
        // Verify that if we have injection information, we're not being injected into a singleton or per thread scoped object
        InstantiationData data = instantiationService.getInstantiationData();
        if (data != null) {
            Injectee injectee = data.getParentInjectee();
            if (injectee != null && injectee.getInjecteeDescriptor().getScopeAnnotation() != RequestScoped.class) {
                return metricName;
            }
        }
        return buildRequestScopedName(metricName, injectionSite, metricType);
    }

    public void setResourceInfoProvider(@NonNull InstantiationService instantiationService) {
        this.instantiationService = instantiationService;
    }

    public void setResourceInfoProvider(@NonNull RequestScope requestScope) {
        this.requestScope = requestScope;
    }

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
    public abstract MetricName buildRequestScopedName(MetricName metricName, AnnotatedElement injectionSite, Type metricType);
}
