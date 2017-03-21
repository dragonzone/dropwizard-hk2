package zone.dragon.dropwizard.metrics.naming;

import org.glassfish.jersey.spi.Contract;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

/**
 * Filter for renaming or tagging metrics
 */
@Contract
@org.jvnet.hk2.annotations.Contract
public interface MetricNameFilter {

    int DEFAULT_NAME_PRIORITY = 4000;
    int OVERRIDE_NAME_PRIORITY = 3000;
    int DEFAULT_TAG_PRIORITY = 2000;
    int OVERRIDE_TAG_PRIORITY = 1000;

    MetricName buildName(MetricName metricName, AnnotatedElement parent, Type metricType);
}
