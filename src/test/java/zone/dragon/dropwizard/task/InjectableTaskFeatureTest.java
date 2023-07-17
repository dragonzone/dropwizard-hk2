package zone.dragon.dropwizard.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import zone.dragon.dropwizard.TestApplication;
import zone.dragon.dropwizard.TestConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
@ExtendWith(DropwizardExtensionsSupport.class)
public class InjectableTaskFeatureTest {
    public static final DropwizardAppExtension<TestConfig> RULE = new DropwizardAppExtension<>(
        TestApplication.class,
        ResourceHelpers.resourceFilePath("config.yaml")
    );

    @Test
    public void testHealthCheckCreated() throws InterruptedException {
        Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");
        String response = client
            .target(String.format("http://localhost:%d/tasks/test-task", RULE.getAdminPort()))
            .request()
            .post(Entity.text(""), String.class);
        assertThat(response).isEqualTo("Executing task test-task, testProperty: testValue" + System.lineSeparator());
    }
}
