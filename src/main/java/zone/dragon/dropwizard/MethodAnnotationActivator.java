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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InstanceLifecycleEvent;
import org.glassfish.hk2.api.InstanceLifecycleEventType;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;

import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * {@link InstanceLifecycleListener} that listens for {@link Singleton singleton} services with methods that are annotated with {@link T};
 * The {@link #activate(ActiveDescriptor, Object, Method, Annotation) activate} method will be invoked once for each method on the service
 * that has the annotation after the service has been created.
 *
 * @param <T>
 *     Annotation type that is used to indicate for which methods {@link #activate(ActiveDescriptor, Object, Method, Annotation) activate}
 *     should be called.
 */
@Singleton
@RequiredArgsConstructor
public abstract class MethodAnnotationActivator<T extends Annotation> implements InstanceLifecycleListener {
    private static final ClassReflectionHelper HELPER = new ClassReflectionHelperImpl();

    private static final Filter SINGLETON_FILTER = descriptor -> Singleton.class.getName().equals(descriptor.getScope());

    /**
     * Annotation type that is used to indicate for which methods {@link #activate(ActiveDescriptor, Object, Method, Annotation) activate}
     * should be called.
     */
    @NonNull
    private final Class<T> annotationType;

    @Override
    public Filter getFilter() {
        return SINGLETON_FILTER;
    }

    /**
     * When a service is created, this is called for each method on the service that is annotated with {@link T}.
     *
     * @param descriptor
     *     Descriptor for the service being created
     * @param service
     *     Instance of the service being created
     * @param method
     *     Method on the service with the annotation
     * @param annotation
     *     Annotation on the {@code method} that binds it to this activator
     */
    protected abstract void activate(ActiveDescriptor<?> descriptor, Object service, Method method, T annotation);

    @Override
    public void lifecycleEvent(InstanceLifecycleEvent lifecycleEvent) {
        if (lifecycleEvent.getEventType() == InstanceLifecycleEventType.POST_PRODUCTION) {
            Object object = lifecycleEvent.getLifecycleObject();
            ActiveDescriptor<?> descriptor = lifecycleEvent.getActiveDescriptor();
            if (object == null) {
                return;
            }
            HELPER.getAllMethods(descriptor.getImplementationClass()).forEach(wrapper -> {
                Method method = wrapper.getMethod();
                T annotation = method.getAnnotation(annotationType);
                if (annotation != null) {
                    method.setAccessible(true);
                    activate(descriptor, object, method, annotation);
                }
            });
        }
    }
}
