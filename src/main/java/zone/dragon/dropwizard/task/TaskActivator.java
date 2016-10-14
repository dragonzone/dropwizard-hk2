package zone.dragon.dropwizard.task;

import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.api.ServiceLocator;
import zone.dragon.dropwizard.ComponentActivator;

import javax.inject.Inject;

/**
 * Activates and initializes all {@link io.dropwizard.servlets.tasks.Task tasks} registered with Jersey and adds them to DropWizard.
 */
@Slf4j
public class TaskActivator extends ComponentActivator {
    private final Environment environment;

    @Inject
    public TaskActivator(ServiceLocator locator, Environment environment) {
        super(locator);
        this.environment = environment;
    }

    @Override
    protected void activateComponents() {
        activate(InjectableTask.class, (name, component) -> {
            log.info("Registering task {}", component.getName());
            environment.admin().addTask(component);
        });
    }
}
