package zone.dragon.dropwizard.lifecycle;

import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.spi.Contract;

import jakarta.inject.Singleton;

/**
 * Marker interface to tag a {@link LifeCycle.Listener} as a Jersey component; Implement this instead of the standard {@link
 * LifeCycle.Listener} to allow registration of the component with Jersey.
 *
 * @author Bryan Harclerode
 */
@Singleton
@Contract
public interface InjectableLifeCycleListener extends LifeCycle.Listener {}
