package zone.dragon.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ImmediateController;
import org.glassfish.hk2.api.ImmediateController.ImmediateServiceState;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.extras.ExtrasUtilities;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.hk2.utilities.binding.BindingBuilder;
import org.glassfish.hk2.utilities.binding.BindingBuilderFactory;
import org.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.glassfish.hk2.utilities.binding.ServiceBindingBuilder;
import org.glassfish.jersey.process.internal.RequestScoped;
import zone.dragon.dropwizard.health.HealthCheckActivator;
import zone.dragon.dropwizard.lifecycle.LifeCycleActivator;
import zone.dragon.dropwizard.metrics.MetricActivator;
import zone.dragon.dropwizard.task.TaskActivator;

import javax.inject.Inject;
import javax.validation.Validator;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Provides integration between DropWizard and HK2, allowing developers to leverage the framework built into Jersey.
 *
 * @author Bryan Harclerode
 */
public class HK2Bundle<T> implements ConfiguredBundle<T>, Binder {
    public static final String SERVICE_LOCATOR = HK2Bundle.class.getName() + "__LOCATOR";

    /**
     * Feature that bridges this bundle's {@link ServiceLocator} with Jersey's. We do this instead of setting the parent because it makes a
     * number of things such as the {@link RequestScoped} context work correctly in either locator.
     */
    @RequiredArgsConstructor(onConstructor = @__(@Inject))
    private static class HK2BridgeFeature implements Feature {
        @NonNull
        private final ServiceLocator serviceLocator;

        @Override
        public boolean configure(FeatureContext context) {
            ServiceLocator bundleLocator = serviceLocator.getService(ServiceLocator.class, SERVICE_LOCATOR);
            ExtrasUtilities.bridgeServiceLocator(bundleLocator, serviceLocator);
            ExtrasUtilities.bridgeServiceLocator(serviceLocator, bundleLocator);
            ExtrasUtilities.enableDefaultInterceptorServiceImplementation(serviceLocator);
            ServiceLocatorUtilities.enableInheritableThreadScope(serviceLocator);
            ServiceLocatorUtilities.enableImmediateScope(serviceLocator);
            return true;
        }
    }

    @Getter
    private final ServiceLocator         locator             = ServiceLocatorFactory.getInstance().create(null);
    private final Set<BindingBuilder<?>> bindings            = Sets.newHashSet();
    private final ImmediateController    immediateController = ServiceLocatorUtilities.enableImmediateScopeSuspended(getLocator());
    private       boolean                bound               = false;

    public HK2Bundle() {
        ExtrasUtilities.enableDefaultInterceptorServiceImplementation(getLocator());
        ExtrasUtilities.enableTopicDistribution(getLocator());
        ServiceLocatorUtilities.enableInheritableThreadScope(getLocator());
        ServiceLocatorUtilities.enablePerThreadScope(getLocator());
    }

    public void autoBind(@NonNull Class<?>... serviceClasses) {
        ServiceLocatorUtilities.addClasses(getLocator(), serviceClasses);
    }

    public void autoBind(@NonNull Factory<?>... factories) {
        ServiceLocatorUtilities.addFactoryConstants(getLocator(), factories);
    }

    public <U> ServiceBindingBuilder<U> bind(@NonNull Class<U> serviceClass) {
        checkState();
        ServiceBindingBuilder<U> builder = BindingBuilderFactory.newBinder(serviceClass);
        bindings.add(builder);
        return builder;
    }

    public <U> ScopedBindingBuilder<U> bind(@NonNull U singleton) {
        checkState();
        ScopedBindingBuilder<U> builder = BindingBuilderFactory.newBinder(singleton);
        bindings.add(builder);
        return builder;
    }

    @Override
    public void bind(DynamicConfiguration config) {
        bindings.forEach(binding -> BindingBuilderFactory.addBinding(binding, config));
    }

    @SuppressWarnings("unchecked")
    public <U> ServiceBindingBuilder<U> bindAsContract(@NonNull Class<U> serviceClass) {
        return bind(serviceClass).to(serviceClass);
    }

    @SuppressWarnings("unchecked")
    public <U> ScopedBindingBuilder<U> bindAsContract(@NonNull U singleton) {
        return bind(singleton).to((Class) singleton.getClass());
    }

    public <U> ServiceBindingBuilder<U> bindFactory(@NonNull Class<? extends Factory<U>> factoryClass) {
        checkState();
        ServiceBindingBuilder<U> builder = BindingBuilderFactory.newFactoryBinder(factoryClass);
        bindings.add(builder);
        return builder;
    }

    public <U> ServiceBindingBuilder<U> bindFactory(
        @NonNull Class<? extends Factory<U>> factoryClass, Class<? extends Annotation> factoryScope
    ) {
        checkState();
        ServiceBindingBuilder<U> builder = BindingBuilderFactory.newFactoryBinder(factoryClass, factoryScope);
        bindings.add(builder);
        return builder;
    }

    public <U> ServiceBindingBuilder<U> bindFactory(@NonNull Factory<U> factory) {
        checkState();
        ServiceBindingBuilder<U> builder = BindingBuilderFactory.newFactoryBinder(factory);
        bindings.add(builder);
        return builder;
    }

    private void checkState() {
        if (bound) {
            throw new IllegalStateException("bind* methods must be called before the run() method");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(@NonNull T configuration, @NonNull Environment environment) {
        // Register Jersey components to activate things when Jersey starts up
        environment.jersey().register(HealthCheckActivator.class);
        environment.jersey().register(MetricActivator.class);
        environment.jersey().register(LifeCycleActivator.class);
        environment.jersey().register(TaskActivator.class);
        environment.jersey().register(HK2BridgeFeature.class);
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(getLocator()).to(ServiceLocator.class).named(SERVICE_LOCATOR);
            }
        });
        // Create bindings for Dropwizard classes
        bind(environment).to(Environment.class);
        bind(environment.healthChecks()).to(HealthCheckRegistry.class);
        bind(environment.lifecycle()).to(LifecycleEnvironment.class);
        bind(environment.metrics()).to(MetricRegistry.class);
        bind(environment.getValidator()).to(Validator.class);
        bind(configuration).to((Class) configuration.getClass()).to(Configuration.class);
        bind(environment.getObjectMapper()).to(ObjectMapper.class);
        // Register all outstanding bindings
        ServiceLocatorUtilities.bind(getLocator(), this);
        bound = true;
        // Add a listener to expose the Jetty Server instance once it is available
        environment.lifecycle().addLifeCycleListener(new AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                if (event instanceof Server) {
                    ServiceLocatorUtilities.addOneConstant(getLocator(), event, null, Server.class);
                    immediateController.setImmediateState(ImmediateServiceState.RUNNING);
                }
            }
        });
        // Make the service locator available to the admin context too.
        environment.getAdminContext().setAttribute(SERVICE_LOCATOR, getLocator());
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) { }
}
