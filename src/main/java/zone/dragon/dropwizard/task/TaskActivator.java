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

import jakarta.inject.Inject;

import org.glassfish.hk2.api.ServiceLocator;

import io.dropwizard.core.setup.Environment;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import zone.dragon.dropwizard.ComponentActivator;

/**
 * Activates and initializes all {@link io.dropwizard.servlets.tasks.Task tasks} registered with Jersey and adds them to DropWizard.
 */
@Slf4j
public class TaskActivator extends ComponentActivator {
    private final Environment environment;

    @Inject
    public TaskActivator(@NonNull ServiceLocator locator, @NonNull Environment environment) {
        super(locator);
        this.environment = environment;
    }

    @Override
    protected void activateComponents() {
        activate(InjectableTask.class, (name, component) -> {
            log.info("Registering task {}", component.getName());
            environment.admin().addTask(component);
        });
    }
}
