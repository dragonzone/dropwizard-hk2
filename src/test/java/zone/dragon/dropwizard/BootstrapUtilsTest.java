package zone.dragon.dropwizard;

import io.dropwizard.Bundle;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Bryan Harclerode
 */
public class BootstrapUtilsTest {
    @Test
    public void testGetBundles() {
        Bootstrap<?> bootstrap = new Bootstrap<>(null);
        Bundle       bundle    = mock(Bundle.class);
        bootstrap.addBundle(bundle);
        //
        List<Bundle> bundles = BootstrapUtils.getBundles(bootstrap);
        //
        assertThat(bundles).isNotNull();
        assertThat(bundles).containsExactly(bundle);
    }

    @Test
    public void testGetConfiguredBundles() {
        Bootstrap<?>     bootstrap = new Bootstrap<>(null);
        ConfiguredBundle<Object> bundle    = mock(ConfiguredBundle.class);
        bootstrap.addBundle(bundle);
        //
        List<ConfiguredBundle> bundles = BootstrapUtils.getConfiguredBundles(bootstrap);
        //
        assertThat(bundles).isNotNull();
        assertThat(bundles).containsExactly(bundle);
    }
}
