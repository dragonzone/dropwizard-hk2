package zone.dragon.dropwizard.lifecycle;

import io.dropwizard.lifecycle.JettyManaged;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.glassfish.hk2.api.ServiceLocator;
import zone.dragon.dropwizard.ComponentActivator;

import javax.inject.Inject;

/**
 * @author Darth Android
 * @date 9/27/2016
 */
@Slf4j
public class LifeCycleActivator extends ComponentActivator {
    private ContainerLifeCycle container;

    @Inject
    public LifeCycleActivator(ServiceLocator locator, Server server) {
        super(locator);
        container = server;
    }

    @Override
    protected void activateComponents() {
        activate(InjectableManaged.class, (name, component) -> container.addBean(new JettyManaged(component)));
        activate(InjectableLifeCycle.class, (name, component) -> container.addBean(component));
        activate(InjectableLifeCycleListener.class, (name, component) -> container.addLifeCycleListener(component));
    }
}
