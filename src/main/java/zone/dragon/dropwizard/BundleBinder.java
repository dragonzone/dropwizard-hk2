package zone.dragon.dropwizard;

import io.dropwizard.setup.Bootstrap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Binds all installed bundles so that they can be injected by type into other components, and adds any bindings exposed from bundles
 * that implement {@link SimpleBinder}
 */
@RequiredArgsConstructor
public class BundleBinder extends AbstractBinder {
    @NonNull
    Bootstrap<?> bootstrap;

    @Override
    public void bind(DynamicConfiguration configuration) {
        super.bind(configuration);
        BootstrapExtensions.getImplementingBundles(bootstrap, SimpleBinder.class).forEach(bundle -> bundle.bind(configuration));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        BootstrapExtensions.getImplementingBundles(bootstrap, Object.class).forEach(bundle -> bind(bundle).to((Class) bundle.getClass()));
    }
}
