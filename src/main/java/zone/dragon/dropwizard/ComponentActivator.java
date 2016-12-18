package zone.dragon.dropwizard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEvent.Type;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.inject.Named;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Darth Android
 * @date 9/27/2016
 */
@Slf4j
@RequiredArgsConstructor
public abstract class ComponentActivator implements ApplicationEventListener {
    protected interface ComponentConsumer<T> extends BiConsumer<String, T> {}

    private final ServiceLocator locator;

    private String getName(Class<?> implementation) {
        Named named = implementation.getAnnotation(Named.class);
        if (named == null || named.value().equals("")) {
            return null;
        }
        return named.value();
    }

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

    protected <T> void activate(Class<T> contract, ComponentConsumer<T> consumer) {
        List<ServiceHandle<T>> handles = locator.getAllServiceHandles(contract);
        handles.forEach(handle -> {
            String name    = handle.getActiveDescriptor().getName();
            T      service = handle.getService();
            if (name == null) {
                name = getName(service.getClass());
            }
            consumer.accept(name, service);
        });
    }

    protected abstract void activateComponents();
}
