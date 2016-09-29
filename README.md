# DropWizard HK2 Integration

This bundle binds and integrates DropWizard's `HealthCheck`, `Metric`, `MetricSet`, `LifeCycle`, `LifeCycle.Listener`, and `Managed`
objects with HK2, allowing these components to be registered directly with Jersey and fully injected with any other services also known to
HK2.

To use this bundle, add it to your application in the initialize method:

    @Override
    public void initialize(Bootstrap<YourConfig> bootstrap) {
        bootstrap.addBundle(new InjectablesBundle<>());
    }

Then, update your health checks, metrics, and managed components to use the custom Jersey component marker interfaces:
`InjectableHealthCheck`, `InjectableManaged`, `InjectableLifeCycle`, `InjectableLifeCycleListener`, `InjectableMetric`, or
`InjectableMetricSet`. These are necessary for Jersey to identify the components as a valid Jersey component, and just extend the
DropWizard interfaces of similar name. Here is an example health check:

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
registered. If a name is not provided, a warning will be printed and a name generated in the format of `ClassName.RandomUUID` to register
 the component under.

Lastly, register your components directly with Jersey:

    @Override
    public void run(TestConfig testConfig, Environment environment) throws Exception {
        environment.jersey().register(YourInjectableHealthCheck.class);
        environment.jersey().register(YourInjectableLifeCycle.class);
    }

They will now be injected and registered properly with DropWizard after HK2 and Jersey are initialized.
