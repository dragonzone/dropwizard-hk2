/*
 * MIT License
 *
 * Copyright (c) 2016-2023 Bryan Harclerode
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package zone.dragon.dropwizard.task;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
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
