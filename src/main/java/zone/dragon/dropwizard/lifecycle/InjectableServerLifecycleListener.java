package zone.dragon.dropwizard.lifecycle;

import org.glassfish.jersey.spi.Contract;

import io.dropwizard.lifecycle.ServerLifecycleListener;
import jakarta.inject.Singleton;

/**
 * Marker interface to tag a {@link ServerLifecycleListener} as a Jersey component; Implement this instead of the standard
 * {@link ServerLifecycleListener} to allow registration of the component with Jersey.
 *
 * @author Bryan Harclerode
 */
@Singleton
@Contract
public interface InjectableServerLifecycleListener extends ServerLifecycleListener {}
