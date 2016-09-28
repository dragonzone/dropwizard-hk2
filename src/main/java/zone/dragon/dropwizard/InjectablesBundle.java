package zone.dragon.dropwizard;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.component.LifeCycle.Listener;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.dragon.dropwizard.health.HealthCheckActivator;
import zone.dragon.dropwizard.lifecycle.LifeCycleActivator;
import zone.dragon.dropwizard.metrics.MetricActivator;

/**
 * @author Darth Android
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
        environment.lifecycle().addLifeCycleListener(new LifecycleLogger());
        environment.jersey().register(new ApplicationEventListener() {
            @Override
            public void onEvent(ApplicationEvent applicationEvent) {
                log.info("Jersey appEvent " + applicationEvent.getType());
            }

            @Override
            public RequestEventListener onRequest(RequestEvent requestEvent) {
                return null;  //TODO Implement
            }
        });
        log.info("Bundle Run");
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) { }

    private class LifecycleLogger implements Listener {
        @Override
        public void lifeCycleStarting(LifeCycle lifeCycle) {
            log.info("Starting {}" + lifeCycle.toString());
        }

        @Override
        public void lifeCycleStarted(LifeCycle lifeCycle) {
            log.info("Started {}" + lifeCycle.toString());
        }

        @Override
        public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
            log.info("Failure {}" + lifeCycle.toString());
        }

        @Override
        public void lifeCycleStopping(LifeCycle lifeCycle) {
            log.info("Stopping {}" + lifeCycle.toString());
        }

        @Override
        public void lifeCycleStopped(LifeCycle lifeCycle) {
            log.info("Stopped {}" + lifeCycle.toString());
        }
    }
}
