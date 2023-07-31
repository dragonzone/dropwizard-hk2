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

import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.jersey.spi.Contract;

/**
 * Factory for building {@link MethodInterceptor constructor interceptors} for methods annotated with {@link T}
 *
 * @param <T>
 *     Annotation type that binds this factory to a particular method
 */
@Contract
@org.jvnet.hk2.annotations.Contract
public interface AnnotatedMethodInterceptorFactory<T extends Annotation> {
    /**
     * Builds a method interceptor for a method based on the marker annotation
     *
     * @param method
     *     Method being intercepted
     * @param annotation
     *     Annotation that marks the method for interception
     *
     * @return Interceptor for this method, or {@code null} if this method should not be intercepted.
     */
    MethodInterceptor provide(Method method, T annotation);
}
