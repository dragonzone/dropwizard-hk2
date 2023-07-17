package zone.dragon.dropwizard.health;

import java.util.UUID;

import org.glassfish.hk2.api.ServiceLocator;

import com.codahale.metrics.health.HealthCheckRegistry;

import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import zone.dragon.dropwizard.ComponentActivator;

/**
 * Activates and initializes all {@link InjectableHealthCheck health checks} registered with Jersey and adds them to DropWizard's
 * {@link HealthCheckRegistry}.
 *
 * @author Bryan Harclerode
 */
@Slf4j
public class HealthCheckActivator extends ComponentActivator {
    private final HealthCheckRegistry registry;

    @Inject
    public HealthCheckActivator(@NonNull ServiceLocator locator, @NonNull HealthCheckRegistry registry) {
        super(locator);
        this.registry = registry;
    }

    @Override
    protected void activateComponents() {
        activate(InjectableHealthCheck.class, (name, component) -> {
            if (name == null) {
                log.warn("Health check {} has no name; Use @Named() to give it one!", component);
                name = String.format("%s.%s", component.getClass().getSimpleName(), UUID.randomUUID());
            }
            log.info("Registering health check {}", name);
            registry.register(name, component);
        });
    }
}
