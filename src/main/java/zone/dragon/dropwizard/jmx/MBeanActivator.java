package zone.dragon.dropwizard.jmx;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.glassfish.hk2.api.ActiveDescriptor;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import zone.dragon.dropwizard.ClassAnnotationActivator;

/**
 * Activator that automatically exposes HK2 singletons over JMX if they have the {@link ManagedObject} annotation
 */
@Singleton
public class MBeanActivator extends ClassAnnotationActivator<ManagedObject> {
    private final MBeanContainer container;

    @Inject
    public MBeanActivator(MBeanContainer container) {
        super(ManagedObject.class);
        this.container = container;
    }

    @Override
    protected void activate(ActiveDescriptor<?> descriptor, Object service, ManagedObject annotation) {
        container.beanAdded(null, service);
    }

    @Override
    protected void deactivate(ActiveDescriptor<?> descriptor, Object service, ManagedObject annotation) {
        container.beanRemoved(null, service);
    }
}
