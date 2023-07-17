package zone.dragon.dropwizard.metrics;

import org.glassfish.jersey.spi.Contract;

import com.codahale.metrics.MetricSet;

import jakarta.inject.Singleton;

/**
 * Marker interface to tag a {@link MetricSet} as a Jersey component; Implement this instead of the standard {@link MetricSet} to allow
 * registration of the component with Jersey.
 *
 * @author Bryan Harclerode
 */
@Singleton
@Contract
public interface InjectableMetricSet extends MetricSet {}
