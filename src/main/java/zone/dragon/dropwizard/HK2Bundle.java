package zone.dragon.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
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
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.hk2.utilities.binding.BindingBuilder;
import org.glassfish.hk2.utilities.binding.BindingBuilderFactory;
import org.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.glassfish.hk2.utilities.binding.ServiceBindingBuilder;
import org.glassfish.jersey.process.internal.RequestScoped;
import zone.dragon.dropwizard.health.HealthCheckActivator;
import zone.dragon.dropwizard.jmx.MBeanActivator;
import zone.dragon.dropwizard.lifecycle.LifeCycleActivator;
import zone.dragon.dropwizard.metrics.MetricActivator;
import zone.dragon.dropwizard.task.TaskActivator;

import javax.inject.Inject;
import javax.validation.Validator;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.lang.annotation.Annotation;

import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.addClasses;

/**
 * Provides integration between DropWizard and HK2, allowing developers to leverage the framework built into Jersey.
 *
 * @author Bryan Harclerode
 */
public class HK2Bundle<T extends Configuration> implements ConfiguredBundle<T> {
    public static final String SERVICE_LOCATOR = HK2Bundle.class.getName() + "__LOCATOR";

    /**
     * Adds this bundle to the given {@link Bootstrap} if it has not already been added by another bundle or the application.
     *
     * @param bootstrap
     *     {@code Bootstrap} to which the {@code HK2Bundle} should be added
     *
     * @return The {@code HK2Bundle} registered with the {@code bootstrap}
     */
    @SuppressWarnings("unchecked")
    public static <T extends Configuration> HK2Bundle<T> addTo(Bootstrap<T> bootstrap) {
        return BootstrapExtensions.addConfiguredBundleIfNotExist(bootstrap, HK2Bundle.class, HK2Bundle::new);
    }

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
    private final ServiceLocator      locator             = ServiceLocatorFactory.getInstance().create(null);
    private final ImmediateController immediateController = ServiceLocatorUtilities.enableImmediateScopeSuspended(getLocator());
    private       BindingBuilder<?>   activeBuilder       = null;
    private       Bootstrap<T>        bootstrap           = null;

    public HK2Bundle() {
        ExtrasUtilities.enableDefaultInterceptorServiceImplementation(getLocator());
        ExtrasUtilities.enableTopicDistribution(getLocator());
        ServiceLocatorUtilities.enableInheritableThreadScope(getLocator());
        ServiceLocatorUtilities.enablePerThreadScope(getLocator());
    }

    public void autoBind(@NonNull Class<?>... serviceClasses) {
        addClasses(getLocator(), serviceClasses);
    }

    public void autoBind(@NonNull Factory<?>... factories) {
        ServiceLocatorUtilities.addFactoryConstants(getLocator(), factories);
    }

    public <U> ServiceBindingBuilder<U> bind(@NonNull Class<U> serviceClass) {
        finishBinding();
        return (ServiceBindingBuilder<U>) (activeBuilder = BindingBuilderFactory.newBinder(serviceClass));
    }

    public <U> ScopedBindingBuilder<U> bind(@NonNull U singleton) {
        finishBinding();
        return (ScopedBindingBuilder<U>) (activeBuilder = BindingBuilderFactory.newBinder(singleton));
    }

    public <U> ServiceBindingBuilder<U> bindAsContract(@NonNull Class<U> serviceClass) {
        return bind(serviceClass).to(serviceClass);
    }

    @SuppressWarnings("unchecked")
    public <U> ScopedBindingBuilder<U> bindAsContract(@NonNull U singleton) {
        return bind(singleton).to((Class) singleton.getClass());
    }

    public <U> ServiceBindingBuilder<U> bindFactory(@NonNull Class<? extends Factory<U>> factoryClass) {
        finishBinding();
        return (ServiceBindingBuilder<U>) (activeBuilder = BindingBuilderFactory.newFactoryBinder(factoryClass));
    }

    public <U> ServiceBindingBuilder<U> bindFactory(
        @NonNull Class<? extends Factory<U>> factoryClass, Class<? extends Annotation> factoryScope
    ) {
        finishBinding();
        return (ServiceBindingBuilder<U>) (activeBuilder = BindingBuilderFactory.newFactoryBinder(factoryClass, factoryScope));
    }

    public <U> ServiceBindingBuilder<U> bindFactory(@NonNull Factory<U> factory) {
        finishBinding();
        return (ServiceBindingBuilder<U>) (activeBuilder = BindingBuilderFactory.newFactoryBinder(factory));
    }

    /**
     * Completes the {@link #activeBuilder} and adds it to the {@link #getLocator() service locator}
     */
    private void finishBinding() {
        if (activeBuilder != null) {
            DynamicConfiguration config = ServiceLocatorUtilities.createDynamicConfiguration(getLocator());
            BindingBuilderFactory.addBinding(activeBuilder, config);
            config.commit();
            activeBuilder = null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(@NonNull T configuration, @NonNull Environment environment) {
        finishBinding();
        // Bridge into Jersey's locator
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(getLocator()).to(ServiceLocator.class).named(SERVICE_LOCATOR);
            }
        });
        // Make the service locator available to the admin context too.
        environment.getAdminContext().setAttribute(SERVICE_LOCATOR, getLocator());
        // Finish configuring HK2 when Jetty starts (after the Application.run() method)
        environment.lifecycle().addLifeCycleListener(new AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                if (event instanceof Server) {
                    finishBinding();
                    ServiceLocatorUtilities.addOneConstant(getLocator(), event, null, Server.class);
                    immediateController.setImmediateState(ImmediateServiceState.RUNNING);
                }
            }
        });
        // Create bindings for Dropwizard classes
        ServiceLocatorUtilities.addOneConstant(getLocator(), environment, null, Environment.class);
        ServiceLocatorUtilities.addOneConstant(getLocator(), environment.healthChecks(), null, HealthCheckRegistry.class);
        ServiceLocatorUtilities.addOneConstant(getLocator(), environment.lifecycle(), null, LifecycleEnvironment.class);
        ServiceLocatorUtilities.addOneConstant(getLocator(), environment.metrics(), null, MetricRegistry.class);
        ServiceLocatorUtilities.addOneConstant(getLocator(), environment.getValidator(), null, Validator.class);
        ServiceLocatorUtilities.addOneConstant(getLocator(), environment.getObjectMapper(), null, ObjectMapper.class);
        ServiceLocatorUtilities.addOneConstant(getLocator(), bootstrap.getApplication(), null, Application.class);
        ServiceLocatorUtilities.addOneConstant(getLocator(), configuration, null, Configuration.class);
        ServiceLocatorUtilities.addOneConstant(getLocator(), configuration, null, bootstrap.getApplication().getConfigurationClass());
        // Grab all bindings from other bundles
        BootstrapExtensions
            .getImplementingBundles(bootstrap, SimpleBinder.class)
            .forEach(bundle -> ServiceLocatorUtilities.bind(getLocator(), bundle));
        BootstrapExtensions
            .getImplementingBundles(bootstrap, Object.class)
            .forEach(bundle -> ServiceLocatorUtilities.addOneConstant(getLocator(), bundle, null, bundle.getClass()));
        // Register Jersey components to activate injectable dropwizard components when Jersey starts up
        environment.jersey().register(HK2BridgeFeature.class);
        environment.jersey().register(HealthCheckActivator.class);
        environment.jersey().register(MetricActivator.class);
        environment.jersey().register(LifeCycleActivator.class);
        environment.jersey().register(TaskActivator.class);
        addClasses(getLocator(), true, MBeanActivator.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        this.bootstrap = (Bootstrap<T>) bootstrap;
    }
}
