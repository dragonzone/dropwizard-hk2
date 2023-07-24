/*
 * MIT License
 *
 * Copyright (c) 2016-2023 Bryan Harclerode
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package zone.dragon.dropwizard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.withSettings;

/**
 * @author Bryan Harclerode
 */
@ExtendWith(DropwizardExtensionsSupport.class)
public class BootstrapExtensionsTest {
    @Test
    public void testAddBundleIfNotExist() {
        ConfiguredBundle newBundle = mock(ConfiguredBundle.class);
        Bootstrap<Configuration> bootstrap = new Bootstrap<>(null);
        //
        ConfiguredBundle addedBundle = BootstrapExtensions.addBundleIfNotExist(bootstrap, ConfiguredBundle.class, () -> newBundle);
        //
        assertThat(addedBundle).isSameAs(newBundle);
        assertThat(BootstrapExtensions.getBundles(bootstrap)).containsExactly(newBundle);
    }

    @Test
    public void testAddBundleIfNotExistNullBootstrap() {
        assertThatThrownBy(() ->
            BootstrapExtensions.addBundleIfNotExist(null, ConfiguredBundle.class, () -> mock(ConfiguredBundle.class))).isInstanceOf(
            NullPointerException.class);
    }

    @Test
    public void testAddBundleIfNotExistNullBundleSupplier() {
        assertThatThrownBy(() ->
            BootstrapExtensions.addBundleIfNotExist(
                new Bootstrap<>(null),
                ConfiguredBundle.class,
                null
            )).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testAddBundleIfNotExistNullBundleType() {
        assertThatThrownBy(() ->
            BootstrapExtensions.addBundleIfNotExist(new Bootstrap<>(null), null, () -> mock(ConfiguredBundle.class))).isInstanceOf(
            NullPointerException.class);
    }

    @Test
    public void testAddBundleIfNotExistPreExisting() {
        Supplier<ConfiguredBundle> bundleSupplier = mock(Supplier.class);
        ConfiguredBundle oldBundle = mock(ConfiguredBundle.class);
        Bootstrap<Configuration> bootstrap = new Bootstrap<>(null);
        bootstrap.addBundle(oldBundle);
        //
        ConfiguredBundle addedBundle = BootstrapExtensions.addBundleIfNotExist(bootstrap, ConfiguredBundle.class, bundleSupplier);
        //
        assertThat(addedBundle).isSameAs(oldBundle);
        assertThat(BootstrapExtensions.getBundles(bootstrap)).containsExactly(oldBundle);
        verifyNoInteractions(bundleSupplier);
    }

    @Test
    public void testGetBundlesWithNull() {
        assertThatThrownBy(() ->
            BootstrapExtensions.getBundles(null)).isInstanceOf(NullPointerException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetConfiguredBundles() {
        Bootstrap<Configuration> bootstrap = new Bootstrap<>(null);
        ConfiguredBundle bundle = mock(ConfiguredBundle.class);
        bootstrap.addBundle(bundle);
        //
        List<ConfiguredBundle<Configuration>> bundles = BootstrapExtensions.getBundles(bootstrap);
        //
        assertThat(bundles).isNotNull();
        assertThat(bundles).containsExactly(bundle);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetImplementingBundles() {
        Bootstrap<?> bootstrap = new Bootstrap<>(null);
        ConfiguredBundle hashSetBundle = (ConfiguredBundle) mock(
            HashSet.class,
            withSettings().extraInterfaces(ConfiguredBundle.class).defaultAnswer(RETURNS_DEEP_STUBS)
        );
        ConfiguredBundle hashMapBundle = (ConfiguredBundle) mock(
            HashMap.class,
            withSettings().extraInterfaces(ConfiguredBundle.class).defaultAnswer(RETURNS_DEEP_STUBS)
        );
        bootstrap.addBundle(hashMapBundle);
        bootstrap.addBundle(hashSetBundle);
        //
        List<Set> setBundles = BootstrapExtensions.getImplementingBundles(bootstrap, Set.class);
        List<Map> mapBundles = BootstrapExtensions.getImplementingBundles(bootstrap, Map.class);
        //
        assertThat(setBundles).isNotNull();
        assertThat(setBundles).containsExactlyInAnyOrder((Set) hashSetBundle);
        assertThat(mapBundles).isNotNull();
        assertThat(mapBundles).containsExactlyInAnyOrder((Map) hashMapBundle);
    }

    @Test
    public void testGetImplementingBundlesNullBootstrap() {
        assertThatThrownBy(() ->
            BootstrapExtensions.getImplementingBundles(null, Object.class)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testGetImplementingBundlesNullType() {
        assertThatThrownBy(() ->
            BootstrapExtensions.getImplementingBundles(new Bootstrap<>(null), null)).isInstanceOf(NullPointerException.class);
    }
}
