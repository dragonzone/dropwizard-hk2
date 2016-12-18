package zone.dragon.dropwizard;

import zone.dragon.dropwizard.health.InjectableHealthCheck;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Darth Android
 * @date 9/24/2016
 */
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
