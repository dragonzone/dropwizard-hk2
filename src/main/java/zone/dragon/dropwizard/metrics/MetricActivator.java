package zone.dragon.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.api.ServiceLocator;
import zone.dragon.dropwizard.ComponentActivator;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Activates all {@link InjectableMetricSet} and {@link InjectableMetric} Jersey components and registers them with DropWizard's {@link
 * MetricRegistry}
 *
 * @author Bryan Harclerode
 */
@Slf4j
public class MetricActivator extends ComponentActivator {
    private final MetricRegistry registry;

    @Inject
    public MetricActivator(@NonNull ServiceLocator locator, @NonNull MetricRegistry registry) {
        super(locator);
        this.registry = registry;
    }

    @Override
    protected void activateComponents() {
        activate(InjectableMetric.class, (name, component) -> {
            if (name == null) {
                log.warn("Metric {} has no name; Use @Named() to give it one!", component);
                name = String.format("%s.%s", component.getClass().getSimpleName(), UUID.randomUUID());
            }
            log.info("Registering metric {}", name);
            registry.register(name, component);
        });
        activate(InjectableMetricSet.class, (name, component) -> registry.registerAll(component));
    }
}
