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

import org.glassfish.hk2.internal.ConstantActiveDescriptor;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import jakarta.validation.Validator;
import lombok.NonNull;

/**
 * This binder makes much of the Dropwizard environment available to HK2 to be injected into components that request it at runtime.
 * Specifically, the following components are bound: <ul> <li>{@link Environment}</li> <li>{@link HealthCheckRegistry}</li>
 * <li>{@link LifecycleEnvironment}</li> <li>{@link MetricRegistry}</li> <li>{@link Configuration}</li>
 * <li>{@link ObjectMapper}</li> <li>{@link Validator}</li>
 * <li>{@link Application}</li></ul>
 */
public class EnvironmentBinder<T extends Configuration> extends AbstractBinder {
    private final Bootstrap<T> bootstrap;

    private final T configuration;

    private final Environment environment;

    /**
     * Creates a new binder that exposes the Dropwizard environment to HK2
     *
     * @param bootstrap
     *     Dropwizard boostrap
     * @param configuration
     *     Dropwizard configuration
     * @param environment
     *     Dropwizard environment
     */
    public EnvironmentBinder(@NonNull Bootstrap bootstrap, @NonNull T configuration, @NonNull Environment environment) {
        this.bootstrap = bootstrap;
        this.configuration = configuration;
        this.environment = environment;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        bind(environment).to(Environment.class);
        bind(environment.healthChecks()).to(HealthCheckRegistry.class);
        bind(environment.lifecycle()).to(LifecycleEnvironment.class);
        bind(environment.metrics()).to(MetricRegistry.class);
        bind(environment.getValidator()).to(Validator.class);
        bind(configuration).to(bootstrap.getApplication().getConfigurationClass()).to(Configuration.class);
        bind(environment.getObjectMapper()).to(ObjectMapper.class);
        bind(bootstrap.getApplication()).to((Class) bootstrap.getApplication().getClass()).to(Application.class);
    }
}
