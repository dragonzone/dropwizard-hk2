package zone.dragon.dropwizard;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * @author Bryan Harclerode
 * @date 9/23/2016
 */
public class TestApplication extends Application<TestConfig> {
    @Override
    public void initialize(Bootstrap<TestConfig> bootstrap) {
        HK2Bundle.addTo(bootstrap);
    }

    @Override
    public void run(TestConfig testConfig, Environment environment) throws Exception {
        environment.jersey().register(TestInjectableHealthCheck.class);
        environment.jersey().register(TestInjectableTask.class);
    }
}
