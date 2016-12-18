package zone.dragon.dropwizard.metrics;

import com.codahale.metrics.Metric;
import org.glassfish.jersey.spi.Contract;

import javax.inject.Singleton;

/**
 * Marker interface to tag a {@link Metric} as a Jersey component; Implement this instead of the standard {@link Metric} to allow
 * registration of the component with Jersey.
 *
 * @author Bryan Harclerode
 */
@Singleton
@Contract
public interface InjectableMetric extends Metric {}
