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

package zone.dragon.dropwizard.health;

import java.util.UUID;

import org.glassfish.hk2.api.ServiceLocator;

import com.codahale.metrics.health.HealthCheckRegistry;

import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import zone.dragon.dropwizard.ComponentActivator;

/**
 * Activates and initializes all {@link InjectableHealthCheck health checks} registered with Jersey and adds them to DropWizard's
 * {@link HealthCheckRegistry}.
 *
 * @author Bryan Harclerode
 */
@Slf4j
public class HealthCheckActivator extends ComponentActivator {
    private final HealthCheckRegistry registry;

    @Inject
    public HealthCheckActivator(@NonNull ServiceLocator locator, @NonNull HealthCheckRegistry registry) {
        super(locator);
        this.registry = registry;
    }

    @Override
    protected void activateComponents() {
        activate(InjectableHealthCheck.class, (name, component) -> {
            if (name == null) {
                log.warn("Health check {} has no name; Use @Named() to give it one!", component);
                name = String.format("%s.%s", component.getClass().getSimpleName(), UUID.randomUUID());
            }
            log.info("Registering health check {}", name);
            registry.register(name, component);
        });
    }
}
