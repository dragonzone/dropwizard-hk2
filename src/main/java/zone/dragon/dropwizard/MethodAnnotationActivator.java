package zone.dragon.dropwizard;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InstanceLifecycleEvent;
import org.glassfish.hk2.api.InstanceLifecycleEventType;
import org.glassfish.hk2.api.InstanceLifecycleListener;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 */
@Singleton
@RequiredArgsConstructor
public abstract class MethodAnnotationActivator<T extends Annotation> implements InstanceLifecycleListener {

    private static final Filter SINGLETON_FILTER = descriptor -> Singleton.class.getName().equals(descriptor.getScope());
    @NonNull
    private final Class<T> annotationType;

    @Override
    public Filter getFilter() {
        return SINGLETON_FILTER;
    }

    @Override
    public void lifecycleEvent(InstanceLifecycleEvent lifecycleEvent) {
        if (lifecycleEvent.getEventType() == InstanceLifecycleEventType.POST_PRODUCTION) {
            Object              object     = lifecycleEvent.getLifecycleObject();
            ActiveDescriptor<?> descriptor = lifecycleEvent.getActiveDescriptor();
            if (object == null) {
                return;
            }
            Class<?> objectClass = object.getClass();
            while (objectClass != null && objectClass != Object.class) {
                for (Method method : objectClass.getDeclaredMethods()) {
                    T annotation = method.getAnnotation(annotationType);
                    if (annotation != null) {
                        method.setAccessible(true);
                        activate((ActiveDescriptor<Object>) descriptor, object, method, annotation);
                    }
                }
                objectClass = objectClass.getSuperclass();
            }
        }
    }

    protected abstract <K> void activate(ActiveDescriptor<K> handle, K instance, Method method, T annotation);
}
