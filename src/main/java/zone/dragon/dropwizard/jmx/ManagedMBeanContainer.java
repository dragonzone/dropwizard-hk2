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

import javax.inject.Inject;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.util.component.Container;
import org.eclipse.jetty.util.component.Container.InheritedListener;

import io.dropwizard.lifecycle.JettyManaged;
import io.dropwizard.lifecycle.Managed;
import lombok.NonNull;

/**
 * Registers {@link Managed} objects as MBeans
 */
public class ManagedMBeanContainer implements InheritedListener {
    private final MBeanContainer jmxContainer;

    @Inject
    public ManagedMBeanContainer(@NonNull MBeanContainer jmxContainer) {
        this.jmxContainer = jmxContainer;
    }

    @Override
    public void beanAdded(Container parent, Object child) {
        if (child instanceof JettyManaged) {
            Managed object = ((JettyManaged) child).getManaged();
            jmxContainer.beanAdded(parent, object);
        }
    }

    @Override
    public void beanRemoved(Container parent, Object child) {
        if (child instanceof JettyManaged) {
            Managed object = ((JettyManaged) child).getManaged();
            jmxContainer.beanRemoved(parent, object);
        }
    }
}
