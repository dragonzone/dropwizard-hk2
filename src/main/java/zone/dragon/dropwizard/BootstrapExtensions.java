package zone.dragon.dropwizard;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.dropwizard.Bundle;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods for interacting with the {@link Bundle bundles} and {@link ConfiguredBundle configured bundles} in a {@link Bootstrap}
 */
@Slf4j
@UtilityClass
public class BootstrapExtensions {
    private final Field CONFIGURED_BUNDLES_FIELD = getBootstrapField("configuredBundles");

    /**
     * Adds a {@link Bundle} to a {@link Bootstrap bootstrap} in an idempotent manner. Use this when multiple bundles may want a specific
     * bundle as a dependency, but don't know if another bundle has already added it to the application.
     *
     * @param bootstrap
     *     {@code Bootstrap} to which the bundle should be added
     * @param bundleType
     *     Type of the bundle to look for when verifying that it hasn't already been added to the {@code bootstrap}
     * @param bundleSupplier
     *     Supplier for the bundle if it has not already been added to the {@code bootstrap}
     * @param <T>
     *     Type of the bundle
     *
     * @return The first bundle of type {@code T} in the {@code bootstrap}
     */
    @Deprecated
    <T extends Bundle> T addBundleIfNotExist(
        @NonNull Bootstrap<?> bootstrap, @NonNull Class<T> bundleType, @NonNull Supplier<T> bundleSupplier
    ) {
        List<T> implementingBundles = getImplementingBundles(bootstrap, bundleType);
        if (implementingBundles.isEmpty()) {
            T bundle = bundleSupplier.get();
            bootstrap.addBundle(bundle);
            return bundle;
        }
        return implementingBundles.get(0);
    }

    /**
     * Adds a {@link ConfiguredBundle} to a {@link Bootstrap bootstrap} in an idempotent manner. Use this when multiple bundles may want a
     * specific bundle as a dependency, but don't know if another bundle has already added it to the application.
     *
     * @param bootstrap
     *     {@code Bootstrap} to which the bundle should be added
     * @param bundleType
     *     Type of the bundle to look for when verifying that it hasn't already been added to the {@code bootstrap}
     * @param bundleSupplier
     *     Supplier for the bundle if it has not already been added to the {@code bootstrap}
     * @param <T>
     *     Type of the bundle
     *
     * @return The first configured bundle of type {@code T} in the {@code bootstrap}
     */
    <U extends Configuration, T extends ConfiguredBundle<U>> T addConfiguredBundleIfNotExist(
        @NonNull Bootstrap<U> bootstrap, @NonNull Class<T> bundleType, @NonNull Supplier<T> bundleSupplier
    ) {
        List<T> implementingBundles = getImplementingBundles(bootstrap, bundleType);
        if (implementingBundles.isEmpty()) {
            T bundle = bundleSupplier.get();
            bootstrap.addBundle(bundle);
            return bundle;
        }
        return implementingBundles.get(0);
    }

    @SneakyThrows
    private Field getBootstrapField(@NonNull String name) {
        try {
            Field field = Bootstrap.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            log.error(
                "Failed to find an accessible field named '{}' on Bootstrap; Is HK2Bundle compatible with this dropwizard version?",
                name,
                e
            );
            throw e;
        }
    }

    /**
     * Retrieves the list of {@link Bundle bundles} registered with a {@link Bootstrap}. Note, this does not return
     * {@link ConfiguredBundle configured bundles}.
     *
     * @param bootstrap
     *     {@code Bootstrap} from which to retrieve bundles
     *
     * @return The bundles in the {@code Bootstrap}
     *
     * @deprecated Use {@link #getConfiguredBundles(Bootstrap)} instead
     *
     * @see #getConfiguredBundles(Bootstrap)
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    @SneakyThrows
    List<Bundle> getBundles(@NonNull Bootstrap<?> bootstrap) {
        return getConfiguredBundles(bootstrap)
            .stream()
            .filter(Bundle.class::isInstance)
            .map(Bundle.class::cast)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of {@link ConfiguredBundle configured bundles} registered with a {@link Bootstrap}. Note, this does not return
     * {@link Bundle bundles}.
     *
     * @param bootstrap
     *     {@code Bootstrap} from which to retrieve configured bundles
     *
     * @return The configured bundles in the {@code Bootstrap}
     *
     * @see #getBundles(Bootstrap)
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    <U extends Configuration> List<ConfiguredBundle<U>> getConfiguredBundles(@NonNull Bootstrap<U> bootstrap) {
        return Collections.unmodifiableList((List<ConfiguredBundle<U>>) CONFIGURED_BUNDLES_FIELD.get(bootstrap));
    }

    /**
     * Return a filtered list of all bundles that have been added to {@link Bootstrap bootstrap} which implement the given {@code type}.
     * This is useful for one bundle to interact with other bundles in the {@link Bundle#run(Environment) run} method.
     *
     * @param bootstrap
     *     {@code Bootstrap} from which to retrieve bundles.
     * @param type
     *     Implemented type by which bundles should be filtered
     * @param <T>
     *     Implemented type by which bundles should be filtered
     *
     * @return A list of all {@link Bundle bundles} and {@link ConfiguredBundle configured bundles} which implement {@code type} and are
     * registered with the {@code bootstrap}
     */
    @SuppressWarnings("unchecked")
    <T> List<T> getImplementingBundles(@NonNull Bootstrap<?> bootstrap, @NonNull Class<T> type) {
        return getConfiguredBundles(bootstrap)
            .stream()
            .filter(bundle -> type.isAssignableFrom(bundle.getClass()))
            .map(type::cast)
            .collect(Collectors.toList());

    }
}
