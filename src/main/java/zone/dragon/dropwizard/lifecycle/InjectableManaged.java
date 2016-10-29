package zone.dragon.dropwizard.lifecycle;

import io.dropwizard.lifecycle.Managed;
import org.glassfish.jersey.spi.Contract;

import javax.inject.Singleton;

/**
 * Marker interface to tag a {@link Managed} as a Jersey component; Implement this instead of the standard {@link Managed} to allow
 * registration of the component with Jersey.
 *
 * @author Bryan Harclerode
 * @date 9/23/2016
 */
@Singleton
@Contract
public interface InjectableManaged extends Managed {}
