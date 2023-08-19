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

package zone.dragon.dropwizard.jmx;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.glassfish.hk2.api.ActiveDescriptor;

import zone.dragon.dropwizard.ClassAnnotationActivator;

/**
 * Activator that automatically exposes HK2 singletons over JMX if they have the {@link ManagedObject} annotation
 */
@Singleton
public class MBeanActivator extends ClassAnnotationActivator<ManagedObject> {
    private final MBeanContainer container;

    @Inject
    public MBeanActivator(MBeanContainer container) {
        super(ManagedObject.class);
        this.container = container;
    }

    @Override
    protected void activate(ActiveDescriptor<?> descriptor, Object service, ManagedObject annotation) {
        container.beanAdded(null, service);
    }

    @Override
    protected void deactivate(ActiveDescriptor<?> descriptor, Object service, ManagedObject annotation) {
        container.beanRemoved(null, service);
    }
}
