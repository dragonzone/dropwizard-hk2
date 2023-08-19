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

package zone.dragon.dropwizard;

import java.util.function.BiConsumer;

import jakarta.inject.Named;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEvent.Type;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for initializing custom Jersey components; Implement the {@link #activateComponents()} to call
 * {@link #activate(Class, ComponentConsumer)} and then retrieve/initialize custom components.
 *
 * @author Bryan Harclerode
 */
@Slf4j
@RequiredArgsConstructor
public abstract class ComponentActivator implements ApplicationEventListener {
    protected interface ComponentConsumer<T> extends BiConsumer<String, T> {}

    /**
     * Tries to reflect the name of a class, as defined by the {@link Named @Named} annotation
     *
     * @param implementation
     *     Class to inspect for a name
     *
     * @return The annotated name for this class, or {@code null} if it does not have one
     */
    private static String getName(Class<?> implementation) {
        Named named = implementation.getAnnotation(Named.class);
        if (named == null || named.value().isEmpty()) {
            return null;
        }
        return named.value();
    }

    @NonNull
    private final ServiceLocator locator;

    /**
     * Finds all Jersey components that provide a specific contract, and invokes a callback with each discovered component to activate it.
     *
     * @param contract
     *     The contract that components must implement
     * @param consumer
     *     Callback to provide for each discovered component
     * @param <T>
     *     Type of the component to find and activate
     */
    protected <T> void activate(@NonNull Class<T> contract, @NonNull ComponentConsumer<T> consumer) {
        locator.getAllServiceHandles(contract).forEach(handle -> {
            String name = handle.getActiveDescriptor().getName();
            T service = handle.getService();
            if (name == null) {
                name = getName(service.getClass());
            }
            consumer.accept(name, service);
        });
    }

    /**
     * Called once all Jersey components are ready and bound; The implementation should use {@link #activate(Class, ComponentConsumer)} to
     * load activate supported components.
     */
    protected abstract void activateComponents();

    @Override
    public void onEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent.getType() == Type.INITIALIZATION_START) {
            // Request all implementations of the contract from HK2 and activate them
            activateComponents();
        }
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return null; // no request processing
    }
}
