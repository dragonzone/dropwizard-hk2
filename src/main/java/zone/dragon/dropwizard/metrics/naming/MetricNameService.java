package zone.dragon.dropwizard.metrics.naming;

import org.jvnet.hk2.annotations.Contract;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

/**
 * Service that generates a metric name for injection sites based on the context of the injection site and any annotations on the
 * injection site.
 */
@Contract
public interface MetricNameService {
    MetricName getMetricName(AnnotatedElement parent, Type metricType);

    String getFormattedMetricName(AnnotatedElement parent, Type metricType);

}
