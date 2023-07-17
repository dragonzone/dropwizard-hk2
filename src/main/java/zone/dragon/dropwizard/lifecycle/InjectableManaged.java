package zone.dragon.dropwizard.lifecycle;

import org.glassfish.jersey.spi.Contract;

import io.dropwizard.lifecycle.Managed;
import jakarta.inject.Singleton;

/**
 * Marker interface to tag a {@link Managed} as a Jersey component; Implement this instead of the standard {@link Managed} to allow
 * registration of the component with Jersey.
 *
 * @author Bryan Harclerode
 */
@Singleton
@Contract
public interface InjectableManaged extends Managed {}
