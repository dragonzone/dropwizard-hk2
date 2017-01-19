package zone.dragon.dropwizard;

import io.dropwizard.Bundle;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * @author Bryan Harclerode
 */
@Slf4j
@UtilityClass
public class BootstrapUtils {
    private final Field BUNDLES_FIELD            = getBootstrapField("bundles");
    private final Field CONFIGURED_BUNDLES_FIELD = getBootstrapField("configuredBundles");

    @SneakyThrows
    private Field getBootstrapField(@NonNull String name) {
        try {
            Field field = Bootstrap.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            log.error("Failed to find an accessible field named '{}}' on Bootstrap", name, e);
            throw e;
        }
    }

    /**
     * Retrieves the list of {@link Bundle bundles} registered with a {@link Bootstrap}. Note, this does not return
     * {@link ConfiguredBundle configured bundles}.
     *
     * @param bootstrap
     *     {@code Bootstrap} from which to retrieve bundles.
     *
     * @return The bundles in the {@code Bootstrap}
     *
     * @see #getConfiguredBundles(Bootstrap)
     * @deprecated This uses reflection to access the list of bundles; It will be removed if dropwizard provides a non-reflection means of
     * accessing {@link Bootstrap#bundles}
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    @SneakyThrows
    List<Bundle> getBundles(@NonNull Bootstrap bootstrap) {
        return Collections.unmodifiableList((List<Bundle>) BUNDLES_FIELD.get(bootstrap));
    }

    /**
     * Retrieves the list of {@link ConfiguredBundle configured bundles} registered with a {@link Bootstrap}. Note, this does not return
     * {@link Bundle bundles}.
     *
     * @param bootstrap
     *     {@code Bootstrap} from which to retrieve configured bundles.
     *
     * @return The configured bundles in the {@code Bootstrap}
     *
     * @see #getBundles(Bootstrap)
     * @deprecated This uses reflection to access the list of configured bundles; It will be removed if dropwizard provides a non-reflection
     * means of accessing {@link Bootstrap#configuredBundles}
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    @SneakyThrows
    List<ConfiguredBundle> getConfiguredBundles(@NonNull Bootstrap bootstrap) {
        return Collections.unmodifiableList((List<ConfiguredBundle>) CONFIGURED_BUNDLES_FIELD.get(bootstrap));
    }
}
