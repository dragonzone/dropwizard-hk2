package zone.dragon.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import lombok.NonNull;
import org.eclipse.jetty.server.Server;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * This binder makes much of the DropWizard environment available to HK2 to be injected into components that request it at runtime.
 * Specifically, the following components are bound: <ul> <li>{@link Environment}</li> <li>{@link HealthCheckRegistry}</li> <li>{@link
 * LifecycleEnvironment}</li> <li>{@link MetricRegistry}</li> <li>{@link Configuration}</li>
 * <li>{@link ObjectMapper}</li> <li>{@link Server}</li></ul>
 */
public class EnvironmentBinder<T> extends AbstractBinder {
    private final T           configuration;
    private final Environment environment;
    private       Server      server;

    /**
     * Creates a new binder that exposes the DropWizard environment to HK2
     *
     * @param configuration
     *     DropWizard configuration
     * @param environment
     *     DropWizard environment
     */
    public EnvironmentBinder(@NonNull T configuration, @NonNull Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
        environment.lifecycle().addServerLifecycleListener(server1 -> server = server1);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        bind(environment).to(Environment.class);
        bind(environment.healthChecks()).to(HealthCheckRegistry.class);
        bind(environment.lifecycle()).to(LifecycleEnvironment.class);
        bind(environment.metrics()).to(MetricRegistry.class);
        bind(configuration).to((Class) configuration.getClass()).to(Configuration.class);
        bind(server).to(Server.class);
        bind(environment.getObjectMapper()).to(ObjectMapper.class);
    }
}
