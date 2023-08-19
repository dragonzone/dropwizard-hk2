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

package zone.dragon.dropwizard.lifecycle;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import zone.dragon.dropwizard.HK2Bundle;
import zone.dragon.dropwizard.TestConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Bryan Harclerode
 * @date 10/29/2016
 */
@ExtendWith(DropwizardExtensionsSupport.class)
public class InjectableManagedTest {
    private static String testValue;

    private static boolean stopped;

    @AfterAll
    public static void verifyStop() {
        assertThat(stopped).isEqualTo(true);
    }

    public static class ManagedApp extends Application<TestConfig> {
        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            HK2Bundle.addTo(bootstrap);
        }

        @Override
        public void run(TestConfig testConfig, Environment environment) throws Exception {
            environment.jersey().register(TestManaged.class);
        }
    }

    public static class TestManaged implements InjectableManaged {
        private final TestConfig config;

        @Inject
        public TestManaged(TestConfig config) {
            this.config = config;
        }

        @Override
        public void start() throws Exception {
            if (testValue != null) {
                fail("Already started");
            }
            testValue = config.getTestProperty();
        }

        @Override
        public void stop() throws Exception {
            if (stopped) {
                fail("Already stopped");
            }
            stopped = true;
        }
    }

    public final DropwizardAppExtension<TestConfig> RULE = new DropwizardAppExtension<>(
        ManagedApp.class,
        ResourceHelpers.resourceFilePath("config.yaml")
    );

    @Test
    public void testServerStartedCalled() {
        assertThat(testValue).isEqualTo("testValue");
    }
}
