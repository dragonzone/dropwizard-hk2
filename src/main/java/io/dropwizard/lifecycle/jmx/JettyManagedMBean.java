package io.dropwizard.lifecycle.jmx;

import io.dropwizard.lifecycle.JettyManaged;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import org.eclipse.jetty.jmx.ObjectMBean;

/**
 * Wrapper that enables JMX for all the {@link JettyManaged} objects that are added to dropwizard's
 * {@link LifecycleEnvironment env.lifecycle()}
 */
public class JettyManagedMBean extends ObjectMBean {
    public JettyManagedMBean(Object managedObject) {
        super(managedObject instanceof JettyManaged ? ((JettyManaged) managedObject).getManaged() : managedObject);
    }
}
