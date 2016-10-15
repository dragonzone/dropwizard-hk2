package zone.dragon.dropwizard;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.dragon.dropwizard.health.HealthCheckActivator;
import zone.dragon.dropwizard.lifecycle.LifeCycleActivator;
import zone.dragon.dropwizard.metrics.MetricActivator;
import zone.dragon.dropwizard.task.TaskActivator;

/**
 * Provides integration between DropWizard and HK2, allowing
 *
 * @author Bryan Harclerode
 * @date 9/23/2016
 */
public class InjectablesBundle<T> implements ConfiguredBundle<T> {
    private static final Logger log = LoggerFactory.getLogger(InjectablesBundle.class);

    @Override
    public void run(T configuration, Environment environment) {
        environment.jersey().register(new EnvironmentBinder<>(configuration, environment));
        environment.jersey().register(HealthCheckActivator.class);
        environment.jersey().register(MetricActivator.class);
        environment.jersey().register(LifeCycleActivator.class);
        environment.jersey().register(TaskActivator.class);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) { }
}
