package zone.dragon.dropwizard.lifecycle;

import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.spi.Contract;

import javax.inject.Singleton;

/**
 * Marker interface to tag a {@link LifeCycle} as a Jersey component; Implement this instead of the standard {@link LifeCycle} to allow
 * registration of the component with Jersey.
 *
 * @author Bryan Harclerode
 * Date 9/23/2016
 */
@Singleton
@Contract
public interface InjectableLifeCycle extends LifeCycle {}
