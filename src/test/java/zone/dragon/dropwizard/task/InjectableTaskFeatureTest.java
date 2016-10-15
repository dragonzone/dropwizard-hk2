package zone.dragon.dropwizard.task;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import zone.dragon.dropwizard.TestApplication;
import zone.dragon.dropwizard.TestConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class InjectableTaskFeatureTest {

    @ClassRule
    public static final DropwizardAppRule<TestConfig> RULE = new DropwizardAppRule<>(TestApplication.class,
                                                                                     ResourceHelpers.resourceFilePath("config.yaml")
    );


    @Test
    public void testHealthCheckCreated() throws InterruptedException {
        Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");

        String response = client.target(
                String.format("http://localhost:%d/tasks/test-task", RULE.getAdminPort()))
                .request()
                .post(Entity.text(""), String.class);

        assertThat(response).isEqualTo("Executing task test-task, testProperty: testValue\n");


    }

}
