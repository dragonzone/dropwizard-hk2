package zone.dragon.dropwizard.lifecycle;

import io.dropwizard.lifecycle.ServerLifecycleListener;
import org.glassfish.jersey.spi.Contract;

import javax.inject.Singleton;

/**
 * Marker interface to tag a {@link ServerLifecycleListener} as a Jersey component; Implement this instead of the standard {@link
 * ServerLifecycleListener} to allow registration of the component with Jersey.
 *
 * @author Bryan Harclerode
 */
@Singleton
@Contract
public interface InjectableServerLifecycleListener extends ServerLifecycleListener {}
