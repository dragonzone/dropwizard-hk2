package zone.dragon.dropwizard;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import zone.dragon.dropwizard.health.InjectableHealthCheck;

/**
 * @author Bryan Harclerode
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
        return Result.healthy(config.getTestProperty());
    }
}
