# Dropwizard HK2 Integration [![Build Status](https://jenkins.dragon.zone/buildStatus/icon?job=dragonzone/dropwizard-hk2/master)](https://jenkins.dragon.zone/blue/organizations/jenkins/dragonzone%2Fdropwizard-hk2/activity?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/zone.dragon.dropwizard/dropwizard-hk2/badge.svg)](https://maven-badges.herokuapp.com/maven-central/zone.dragon.dropwizard/dropwizard-hk2/) [![Javadoc](https://javadoc.io/badge2/zone.dragon.dropwizard/dropwizard-hk2/javadoc.svg)](http://www.javadoc.io/doc/zone.dragon.dropwizard/dropwizard-hk2)

This bundle binds and integrates Dropwizard's `HealthCheck`, `Metric`, `MetricSet`, `LifeCycle`, `LifeCycle.Listener`, and `Managed`
objects with HK2, allowing these components to be registered directly with Jersey and fully injected with any other services also known to
HK2. Additionally, it provides access to the Jersey `ServiceLocator` from the admin `ServletContext`, default bindings for many Dropwizard
components such as the `Validator`, `ObjectMapper`, `MetricRegistry`, `HealthCheckRegistry`, your `Application`, your `Configuration`, the
`Environment` and `LifecycleEnvironment`, as well as each bundle added to your application.

To use this bundle, add it to your application in the initialize method:

    @Override
    public void initialize(Bootstrap<YourConfig> bootstrap) {
        // This ensures there's only ever one HK2 bundle; don't use bootstrap.addBundle(new HK2Bundle<>());
        HK2Bundle.addTo(bootstrap);
    }

Then, update your health checks, metrics, and managed components to use the custom Jersey component marker interfaces:
`InjectableHealthCheck`, `InjectableManaged`, `InjectableLifeCycle`, `InjectableLifeCycleListener`, `InjectableMetric`, or
`InjectableMetricSet`. These are necessary for Jersey to identify the components as a valid Jersey component, and just extend the
Dropwizard interfaces of similar name. Here is an example health check:

    @Named("TestHealthCheck")
    public class TestInjectableHealthCheck extends InjectableHealthCheck {
        private final TestConfig config;

        @Inject
        public TestInjectableHealthCheck(TestConfig config) {
            this.config = config;
        }

        public TestConfig getConfig() {
            return config;
        }

        @Override
        protected Result check() throws Exception {
            return Result.healthy();
        }
    }

Health checks and Metrics can also make use of the `@Named()` annotation to identify the name under which the component should be
registered. If a name is not provided, a warning will be printed and a name generated in the format of `ClassName.RandomUUID` under which
the component will be registered.

Lastly, register your components directly with Jersey:

    @Override
    public void run(TestConfig testConfig, Environment environment) throws Exception {
        environment.jersey().register(YourInjectableHealthCheck.class);
        environment.jersey().register(YourInjectableLifeCycle.class);
    }

They will now be injected and registered properly with Dropwizard after HK2 and Jersey are initialized.

To access the `ServiceLocator` from the admin `ServletContext`, query the context attribute identified by `HK2Bundle.SERVICE_LOCATOR`:

    ServiceLocator locator = (ServiceLocator) getServletConfig().getServletContext().getAttribute(HK2Bundle.SERVICE_LOCATOR);

`RequestScoped` bindings are unavailable in the admin context, but singletons and other scopes are available.
