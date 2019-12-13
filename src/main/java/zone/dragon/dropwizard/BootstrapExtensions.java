package zone.dragon.dropwizard;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods for interacting with the {@link ConfiguredBundle configured bundles} in a {@link Bootstrap}
 */
@Slf4j
@UtilityClass
public class BootstrapExtensions {
    private final Field CONFIGURED_BUNDLES_FIELD = getBootstrapField("configuredBundles");

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
    <U extends Configuration, T extends ConfiguredBundle<U>> T addBundleIfNotExist(
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
     * Retrieves the list of {@link ConfiguredBundle configured bundles} registered with a {@link Bootstrap}.
     *
     * @param bootstrap
     *     {@code Bootstrap} from which to retrieve configured bundles
     *
     * @return The configured bundles in the {@code Bootstrap}
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    <U extends Configuration> List<ConfiguredBundle<U>> getBundles(@NonNull Bootstrap<U> bootstrap) {
        return Collections.unmodifiableList((List<ConfiguredBundle<U>>) CONFIGURED_BUNDLES_FIELD.get(bootstrap));
    }

    /**
     * Return a filtered list of all bundles that have been added to {@link Bootstrap bootstrap} which implement the given {@code type}.
     * This is useful for one bundle to interact with other bundles in the {@link ConfiguredBundle#run(T,Environment) run} method.
     *
     * @param bootstrap
     *     {@code Bootstrap} from which to retrieve bundles.
     * @param type
     *     Implemented type by which bundles should be filtered
     * @param <T>
     *     Implemented type by which bundles should be filtered
     *
     * @return A list of all {@link ConfiguredBundle configured bundles} which implement {@code type} and are
     * registered with the {@code bootstrap}
     */
    @SuppressWarnings("unchecked")
    <T> List<T> getImplementingBundles(@NonNull Bootstrap<?> bootstrap, @NonNull Class<T> type) {
        return getBundles(bootstrap)
            .stream()
            .filter(bundle -> type.isAssignableFrom(bundle.getClass()))
            .map(type::cast)
            .collect(Collectors.toList());

    }
}
