package zone.dragon.dropwizard;

import com.google.common.collect.ImmutableMultimap;
import zone.dragon.dropwizard.task.InjectableTask;

import javax.inject.Inject;
import java.io.PrintWriter;

public class TestInjectableTask extends InjectableTask {
    private TestConfig config;

    @Inject
    protected TestInjectableTask(TestConfig config) {
        super("test-task");
        this.config = config;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> immutableMultimap, PrintWriter output) throws Exception {
        output.println("Executing task " + getName() + ", testProperty: " + config.getTestProperty());
        output.flush();
    }
}
