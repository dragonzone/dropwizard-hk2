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

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import io.dropwizard.core.setup.Bootstrap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Binds all installed bundles so that they can be injected by type into other components, and adds any bindings exposed from bundles that
 * implement {@link SimpleBinder}
 */
@RequiredArgsConstructor
public class BundleBinder extends AbstractBinder {
    @NonNull
    Bootstrap<?> bootstrap;

    @Override
    public void bind(DynamicConfiguration configuration) {
        super.bind(configuration);
        BootstrapExtensions.getImplementingBundles(bootstrap, SimpleBinder.class).forEach(bundle -> bundle.bind(configuration));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        BootstrapExtensions.getImplementingBundles(bootstrap, Object.class).forEach(bundle -> bind(bundle).to((Class) bundle.getClass()));
    }
}
