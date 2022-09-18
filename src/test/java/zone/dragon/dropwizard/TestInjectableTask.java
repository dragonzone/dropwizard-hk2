package zone.dragon.dropwizard;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import zone.dragon.dropwizard.task.InjectableTask;

public class TestInjectableTask extends InjectableTask {
    private TestConfig config;

    @Inject
    protected TestInjectableTask(TestConfig config) {
        super("test-task");
        this.config = config;
    }

    @Override
    public void execute(Map<String, List<String>> immutableMultimap, PrintWriter output) throws Exception {
        output.println("Executing task " + getName() + ", testProperty: " + config.getTestProperty());
        output.flush();
    }
}
