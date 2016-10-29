package zone.dragon.dropwizard.task;

import io.dropwizard.servlets.tasks.Task;
import org.glassfish.jersey.spi.Contract;

import javax.inject.Singleton;

/**
 * Marker class to tag a {@link io.dropwizard.servlets.tasks.Task} as a Jersey component; Extend this instead of the standard {@link
 * io.dropwizard.servlets.tasks.Task} to allow registration of the component with Jersey.
 */
@Contract
@Singleton
public abstract class InjectableTask extends Task {
    protected InjectableTask(String name) {
        super(name);
    }
}
