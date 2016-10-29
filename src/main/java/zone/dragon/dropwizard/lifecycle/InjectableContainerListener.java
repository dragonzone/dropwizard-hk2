package zone.dragon.dropwizard.lifecycle;

import org.eclipse.jetty.util.component.Container;
import org.glassfish.jersey.spi.Contract;

import javax.inject.Singleton;

/**
 * Marker interface to tag a {@link Container.Listener} as a Jersey component; Implement this instead of the standard {@link
 * Container.Listener} to allow registration of the component with Jersey.
 *
 * @author Bryan Harclerode
 * @date 10/29/2016
 */
@Singleton
@Contract
public interface InjectableContainerListener extends Container.Listener {}
