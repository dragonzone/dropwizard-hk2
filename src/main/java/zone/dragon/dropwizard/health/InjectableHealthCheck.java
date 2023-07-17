package zone.dragon.dropwizard.health;

import org.glassfish.jersey.spi.Contract;

import com.codahale.metrics.health.HealthCheck;

import jakarta.inject.Singleton;

/**
 * Marker class to tag a {@link HealthCheck} as a Jersey component; Extend this instead of the standard {@link HealthCheck} to allow
 * registration of the component with Jersey.
 *
 * @author Bryan Harclerode
 */
@Contract
@Singleton
public abstract class InjectableHealthCheck extends HealthCheck {}
