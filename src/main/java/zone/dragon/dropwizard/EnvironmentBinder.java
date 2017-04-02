package zone.dragon.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.NonNull;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.validation.Validator;

/**
 * This binder makes much of the Dropwizard environment available to HK2 to be injected into components that request it at runtime.
 * Specifically, the following components are bound: <ul> <li>{@link Environment}</li> <li>{@link HealthCheckRegistry}</li> <li>{@link
 * LifecycleEnvironment}</li> <li>{@link MetricRegistry}</li> <li>{@link Configuration}</li>
 * <li>{@link ObjectMapper}</li> <li>{@link Validator}</li>
 * <li>{@link Application}</li></ul>
 *
 */
public class EnvironmentBinder<T> extends AbstractBinder {
    private final Bootstrap   bootstrap;
    private final T           configuration;
    private final Environment environment;

    /**
     * Creates a new binder that exposes the Dropwizard environment to HK2
     *
     * @param bootstrap
     *     Dropwizard boostrap
     * @param configuration
     *     Dropwizard configuration
     * @param environment
     *     Dropwizard environment
     */
    public EnvironmentBinder(@NonNull Bootstrap bootstrap, @NonNull T configuration, @NonNull Environment environment) {
        this.bootstrap = bootstrap;
        this.configuration = configuration;
        this.environment = environment;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        bind(environment).to(Environment.class);
        bind(environment.healthChecks()).to(HealthCheckRegistry.class);
        bind(environment.lifecycle()).to(LifecycleEnvironment.class);
        bind(environment.metrics()).to(MetricRegistry.class);
        bind(environment.getValidator()).to(Validator.class);
        bind(configuration).to(bootstrap.getApplication().getConfigurationClass()).to(Configuration.class);
        bind(environment.getObjectMapper()).to(ObjectMapper.class);
        bind(bootstrap.getApplication()).to((Class) bootstrap.getApplication().getClass()).to(Application.class);
    }
}
