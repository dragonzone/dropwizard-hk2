package zone.dragon.dropwizard.jmx;

import io.dropwizard.lifecycle.JettyManaged;
import io.dropwizard.lifecycle.Managed;
import lombok.NonNull;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.util.component.Container;
import org.eclipse.jetty.util.component.Container.InheritedListener;

import jakarta.inject.Inject;

/**
 * Registers {@link Managed} objects as MBeans
 */
public class ManagedMBeanContainer implements InheritedListener {
    private final MBeanContainer jmxContainer;

    @Inject
    public ManagedMBeanContainer(@NonNull MBeanContainer jmxContainer) {
        this.jmxContainer = jmxContainer;
    }

    @Override
    public void beanAdded(Container parent, Object child) {
        if (child instanceof JettyManaged) {
            Managed object = ((JettyManaged) child).getManaged();
            jmxContainer.beanAdded(parent, object);
        }
    }

    @Override
    public void beanRemoved(Container parent, Object child) {
        if (child instanceof JettyManaged) {
            Managed object = ((JettyManaged) child).getManaged();
            jmxContainer.beanRemoved(parent, object);
        }
    }
}
