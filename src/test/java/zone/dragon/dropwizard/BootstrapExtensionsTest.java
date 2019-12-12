package zone.dragon.dropwizard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.junit.Test;

import io.dropwizard.Bundle;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.withSettings;

/**
 * @author Bryan Harclerode
 */
public class BootstrapExtensionsTest {
    @Test
    public void testAddBundleIfNotExist() {
        Bundle       newBundle = mock(Bundle.class);
        Bootstrap<Configuration> bootstrap = new Bootstrap<>(null);
        //
        Bundle addedBundle = BootstrapExtensions.addBundleIfNotExist(bootstrap, Bundle.class, () -> newBundle);
        //
        assertThat(addedBundle).isSameAs(newBundle);
        assertThat(BootstrapExtensions.getBundles(bootstrap)).containsExactly(newBundle);
    }

    @Test(expected = NullPointerException.class)
    public void testAddBundleIfNotExistNullBootstrap() {
        BootstrapExtensions.addBundleIfNotExist(null, Bundle.class, () -> mock(Bundle.class));
    }

    @Test(expected = NullPointerException.class)
    public void testAddBundleIfNotExistNullBundleSupplier() {
        BootstrapExtensions.addBundleIfNotExist(new Bootstrap<>(null), Bundle.class, null);
    }

    @Test(expected = NullPointerException.class)
    public void testAddBundleIfNotExistNullBundleType() {
        BootstrapExtensions.addBundleIfNotExist(new Bootstrap<>(null), null, () -> mock(Bundle.class));
    }

    @Test
    public void testAddBundleIfNotExistPreExisting() {
        Supplier<Bundle> bundleSupplier = mock(Supplier.class);
        Bundle           oldBundle      = mock(Bundle.class);
        Bootstrap<Configuration>     bootstrap      = new Bootstrap<>(null);
        bootstrap.addBundle(oldBundle);
        //
        Bundle addedBundle = BootstrapExtensions.addBundleIfNotExist(bootstrap, Bundle.class, bundleSupplier);
        //
        assertThat(addedBundle).isSameAs(oldBundle);
        assertThat(BootstrapExtensions.getBundles(bootstrap)).containsExactly(oldBundle);
        verifyZeroInteractions(bundleSupplier);
    }

    @Test
    public void testGetBundles() {
        Bootstrap<Configuration> bootstrap = new Bootstrap<>(null);
        Bundle       bundle    = mock(Bundle.class);
        bootstrap.addBundle(bundle);
        //
        List<Bundle> bundles = BootstrapExtensions.getBundles(bootstrap);
        //
        assertThat(bundles).isNotNull();
        assertThat(bundles).containsExactly(bundle);
    }

    @Test(expected = NullPointerException.class)
    public void testGetBundlesWithNull() {
        BootstrapExtensions.getBundles(null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetConfiguredBundles() {
        Bootstrap<Configuration>     bootstrap = new Bootstrap<>(null);
        ConfiguredBundle bundle    = mock(ConfiguredBundle.class);
        bootstrap.addBundle(bundle);
        //
        List<ConfiguredBundle<Configuration>> bundles = BootstrapExtensions.getBundles(bootstrap);
        //
        assertThat(bundles).isNotNull();
        assertThat(bundles).containsExactly(bundle);
    }

    @Test(expected = NullPointerException.class)
    public void testGetConfiguredBundlesWithNull() {
        BootstrapExtensions.getBundles(null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetImplementingBundles() {
        Bootstrap<?>     bootstrap     = new Bootstrap<>(null);
        ConfiguredBundle hashSetBundle = (ConfiguredBundle) mock(HashSet.class, withSettings().extraInterfaces(ConfiguredBundle.class).defaultAnswer(RETURNS_DEEP_STUBS));
        ConfiguredBundle hashMapBundle = (ConfiguredBundle) mock(HashMap.class, withSettings().extraInterfaces(ConfiguredBundle.class).defaultAnswer(RETURNS_DEEP_STUBS));
        Bundle           treeSetBundle = (Bundle) mock(TreeSet.class, withSettings().extraInterfaces(Bundle.class).defaultAnswer(RETURNS_DEEP_STUBS));
        Bundle           treeMapBundle = (Bundle) mock(TreeMap.class, withSettings().extraInterfaces(Bundle.class).defaultAnswer(RETURNS_DEEP_STUBS));
        bootstrap.addBundle(hashMapBundle);
        bootstrap.addBundle(hashSetBundle);
        bootstrap.addBundle(treeMapBundle);
        bootstrap.addBundle(treeSetBundle);
        //
        List<Set> setBundles = BootstrapExtensions.getImplementingBundles(bootstrap, Set.class);
        List<Map> mapBundles = BootstrapExtensions.getImplementingBundles(bootstrap, Map.class);
        //
        assertThat(setBundles).isNotNull();
        assertThat(setBundles).containsExactlyInAnyOrder((Set) hashSetBundle, (Set) treeSetBundle);
        assertThat(mapBundles).isNotNull();
        assertThat(mapBundles).containsExactlyInAnyOrder((Map) hashMapBundle, (Map) treeMapBundle);
    }

    @Test(expected = NullPointerException.class)
    public void testGetImplementingBundlesNullBootstrap() {
        BootstrapExtensions.getImplementingBundles(null, Object.class);
    }

    @Test(expected = NullPointerException.class)
    public void testGetImplementingBundlesNullType() {
        BootstrapExtensions.getImplementingBundles(new Bootstrap<>(null), null);
    }
}
