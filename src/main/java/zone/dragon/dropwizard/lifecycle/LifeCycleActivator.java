package zone.dragon.dropwizard.lifecycle;

import io.dropwizard.lifecycle.JettyManaged;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.glassfish.hk2.api.ServiceLocator;
import zone.dragon.dropwizard.ComponentActivator;

import javax.inject.Inject;

/**
 * Activates {@link InjectableLifeCycle}, {@link InjectableLifeCycleListener}, and {@link InjectableManaged} components registered with
 * Jersey and adds them to Jetty
 *
 * @author Bryan Harclerode
 * Date 9/27/2016
 */
@Slf4j
public class LifeCycleActivator extends ComponentActivator {
    private Server container;

    @Inject
    public LifeCycleActivator(@NonNull ServiceLocator locator, @NonNull Server server) {
        super(locator);
        container = server;
    }

    @Override
    protected void activateComponents() {
        activate(InjectableContainerListener.class, (name, component) -> container.addEventListener(component));
        activate(InjectableManaged.class, (name, component) -> container.addBean(new JettyManaged(component)));
        activate(InjectableLifeCycle.class, (name, component) -> container.addBean(component));
        activate(InjectableLifeCycleListener.class, (name, component) -> {
            container.addLifeCycleListener(component);
            // synthesize starting and started event since we've already begun.
            component.lifeCycleStarting(container);
        });
        activate(InjectableServerLifecycleListener.class, (name, component) -> component.serverStarted(container));
    }
}
