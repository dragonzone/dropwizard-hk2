package zone.dragon.dropwizard;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * @author Bryan Harclerode
 */
public interface SimpleBinder extends Binder {
    @Override
    default void bind(DynamicConfiguration config) {
        new AbstractBinder() {
            @Override
            protected void configure() {
                configureBindings(this);
            }
        }.bind(config);
    }

    void configureBindings(AbstractBinder config);
}
