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

import jakarta.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InstanceLifecycleEvent;
import org.glassfish.hk2.api.InstanceLifecycleEventType;
import org.glassfish.hk2.api.InstanceLifecycleListener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * {@link InstanceLifecycleListener} that listens for {@link Singleton singleton} services that are annotated with {@link T}; The
 * {@link #activate(ActiveDescriptor, Object, Annotation) activate} method will be invoked for the service that has the annotation after the
 * service has been created.
 *
 * @param <T>
 *     Annotation type that is used to indicate if the {@link #activate(ActiveDescriptor, Object, Annotation) activate} should be called.
 */
@Singleton
@RequiredArgsConstructor
public abstract class ClassAnnotationActivator<T extends Annotation> implements InstanceLifecycleListener {
    private static final Filter SINGLETON_FILTER = descriptor -> Singleton.class.getName().equals(descriptor.getScope());

    /**
     * Annotation type that is used to indicate if the {@link #activate(ActiveDescriptor, Object, Annotation) activate} should be called.
     */
    @NonNull
    private final Class<T> annotationType;

    @Override
    public Filter getFilter() {
        return SINGLETON_FILTER;
    }

    /**
     * When a service is created, this is called if the service is annotated with {@link T}.
     *
     * @param descriptor
     *     Descriptor for the service being created
     * @param service
     *     Instance of the service being created
     * @param annotation
     *     Annotation on the service that binds it to this activator
     */
    protected abstract void activate(ActiveDescriptor<?> descriptor, Object service, T annotation);

    /**
     * When a service is disposed, this is called if the service is annotated with {@link T}.
     *
     * @param descriptor
     *     Descriptor for the service being disposed
     * @param service
     *     Instance of the service being disposed
     * @param annotation
     *     Annotation on the service that binds it to this activator
     */
    protected void deactivate(ActiveDescriptor<?> descriptor, Object service, T annotation) {}

    @Override
    public void lifecycleEvent(InstanceLifecycleEvent lifecycleEvent) {
        if (lifecycleEvent.getEventType() == InstanceLifecycleEventType.POST_PRODUCTION) {
            Object object = lifecycleEvent.getLifecycleObject();
            ActiveDescriptor<?> descriptor = lifecycleEvent.getActiveDescriptor();
            if (object == null) {
                return;
            }
            T annotation = descriptor.getImplementationClass().getAnnotation(annotationType);
            if (annotation != null) {
                activate(descriptor, object, annotation);
            }
        } else if (lifecycleEvent.getEventType() == InstanceLifecycleEventType.PRE_DESTRUCTION) {
            Object object = lifecycleEvent.getLifecycleObject();
            ActiveDescriptor<?> descriptor = lifecycleEvent.getActiveDescriptor();
            if (object == null) {
                return;
            }
            T annotation = object.getClass().getAnnotation(annotationType);
            if (annotation != null) {
                deactivate(descriptor, object, annotation);
            }
        }
    }
}
