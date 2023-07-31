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

import org.eclipse.jetty.server.Server;
import org.glassfish.hk2.api.ServiceLocator;

import io.dropwizard.lifecycle.JettyManaged;
import jakarta.inject.Inject;
import lombok.NonNull;
import zone.dragon.dropwizard.ComponentActivator;

/**
 * Activates {@link InjectableLifeCycle}, {@link InjectableLifeCycleListener}, and {@link InjectableManaged} components registered with
 * Jersey and adds them to Jetty
 *
 * @author Bryan Harclerode
 */
public class LifeCycleActivator extends ComponentActivator {
    private final Server container;

    @Inject
    public LifeCycleActivator(@NonNull ServiceLocator locator, @NonNull Server server) {
        super(locator);
        container = server;
    }

    @Override
    protected void activateComponents() {
        activate(InjectableContainerListener.class, (name, component) -> container.addEventListener(component));
        activate(InjectableManaged.class, (name, component) -> container.addBean(new JettyManaged(component)));
        activate(InjectableLifeCycle.class, (name, component) -> container.addBean(component));
        activate(InjectableLifeCycleListener.class, (name, component) -> {
            container.addEventListener(component);
            // synthesize starting and started event since we've already begun.
            component.lifeCycleStarting(container);
        });
        activate(InjectableServerLifecycleListener.class, (name, component) -> component.serverStarted(container));
    }
}
