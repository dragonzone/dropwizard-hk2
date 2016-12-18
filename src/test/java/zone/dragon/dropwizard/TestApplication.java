package zone.dragon.dropwizard;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Darth Android
 * @date 9/23/2016
 */
public class TestApplication extends Application<TestConfig> {
    private static final Logger log = LoggerFactory.getLogger(TestApplication.class);

    @Override
    public void initialize(Bootstrap<TestConfig> bootstrap) {
        bootstrap.addBundle(new InjectablesBundle());
    }

    @Override
    public void run(TestConfig testConfig, Environment environment) throws Exception {
        environment.jersey().register(TestInjectableHealthCheck.class);
        log.info("Run method");
    }

}
