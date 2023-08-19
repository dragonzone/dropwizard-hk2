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
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InterceptionService;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

import com.google.common.collect.Lists;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Interception service that binds method and constructor interceptors by annotation. This service allows for interception based on any
 * annotation, and additionally allows for interceptors to be customized based upon properties in the annotation on each method or
 * constructor.
 *
 * @see AnnotatedMethodInterceptorFactory
 * @see AnnotatedConstructorInterceptorFactory
 */
@Slf4j
@Singleton
@Visibility(DescriptorVisibility.LOCAL)
public class AnnotationInterceptionService implements InterceptionService {
    protected interface InterceptorFactory<F, S, T, R> {
        R apply(F first, S second, T third);
    }

    private final IterableProvider<AnnotatedMethodInterceptorFactory<?>> annotatedMethodInterceptorFactories;

    private final IterableProvider<AnnotatedConstructorInterceptorFactory<?>> annotatedConstructorInterceptorFactories;

    private final ServiceLocator locator;

    @Inject
    public AnnotationInterceptionService(
        @NonNull IterableProvider<AnnotatedMethodInterceptorFactory<?>> annotatedMethodInterceptorFactories,
        @NonNull IterableProvider<AnnotatedConstructorInterceptorFactory<?>> annotatedConstructorInterceptorFactories,
        @NonNull ServiceLocator locator
    ) {
        this.annotatedMethodInterceptorFactories = annotatedMethodInterceptorFactories;
        this.annotatedConstructorInterceptorFactories = annotatedConstructorInterceptorFactories;
        this.locator = locator;
    }

    @Override
    public Filter getDescriptorFilter() {
        return descriptor -> true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<MethodInterceptor> getMethodInterceptors(Method method) {
        return getInterceptors(
            method,
            annotatedMethodInterceptorFactories,
            AnnotatedMethodInterceptorFactory.class,
            AnnotatedMethodInterceptorFactory::provide
        );
    }

    @Override
    public List<ConstructorInterceptor> getConstructorInterceptors(Constructor<?> constructor) {
        return getInterceptors(
            constructor,
            annotatedConstructorInterceptorFactories,
            AnnotatedConstructorInterceptorFactory.class,
            AnnotatedConstructorInterceptorFactory::provide
        );
    }

    protected <T extends Executable, I extends Interceptor, F> List<I> getInterceptors(
        T interceptee,
        IterableProvider<? extends F> factoryProviders,
        Class<F> interceptorType,
        InterceptorFactory<F, T, Annotation, I> interceptorProvider
    ) {
        List<I> interceptors = Lists.newArrayList();
        factoryProviders.handleIterator().forEach(handle -> {
            // Make sure descriptor is fully reified
            ActiveDescriptor<?> descriptor = handle.getActiveDescriptor();
            if (!descriptor.isReified()) {
                descriptor = locator.reifyDescriptor(descriptor);
            }
            // Check the type of annotation supported by the factory
            for (Type contract : descriptor.getContractTypes()) {
                if (ReflectionHelper.getRawClass(contract) == interceptorType) {
                    Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) ReflectionHelper.getRawClass(
                        ReflectionHelper.getFirstTypeArgument(contract));
                    // Might be Object.class if getRawClass found an unbound type variable or wildcard
                    if (!Annotation.class.isAssignableFrom(annotationClass)) {
                        log.warn("Unable to determine annotation binder from contract type {}", annotationClass);
                        return;
                    }
                    Annotation ann = interceptee.getAnnotation(annotationClass);
                    if (ann == null) {
                        ann = interceptee.getDeclaringClass().getAnnotation(annotationClass);
                    }
                    if (ann != null) {
                        // Create the factory and produce an interceptor
                        F factory = handle.getService();
                        I interceptor = interceptorProvider.apply(factory, interceptee, ann);
                        if (interceptor != null) {
                            interceptors.add(interceptor);
                        }
                    }
                    return;
                }
            }
        });
        return interceptors;
    }
}
