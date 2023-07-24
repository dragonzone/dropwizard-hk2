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

package zone.dragon.dropwizard.metrics;

import java.util.UUID;

import org.glassfish.hk2.api.ServiceLocator;

import com.codahale.metrics.MetricRegistry;

import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import zone.dragon.dropwizard.ComponentActivator;

/**
 * Activates all {@link InjectableMetricSet} and {@link InjectableMetric} Jersey components and registers them with DropWizard's
 * {@link MetricRegistry}
 *
 * @author Bryan Harclerode
 */
@Slf4j
public class MetricActivator extends ComponentActivator {
    private final MetricRegistry registry;

    @Inject
    public MetricActivator(@NonNull ServiceLocator locator, @NonNull MetricRegistry registry) {
        super(locator);
        this.registry = registry;
    }

    @Override
    protected void activateComponents() {
        activate(InjectableMetric.class, (name, component) -> {
            if (name == null) {
                log.warn("Metric {} has no name; Use @Named() to give it one!", component);
                name = String.format("%s.%s", component.getClass().getSimpleName(), UUID.randomUUID());
            }
            log.info("Registering metric {}", name);
            registry.register(name, component);
        });
        activate(InjectableMetricSet.class, (name, component) -> registry.registerAll(component));
    }
}
