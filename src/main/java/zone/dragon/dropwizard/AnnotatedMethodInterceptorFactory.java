package zone.dragon.dropwizard;

import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.jersey.spi.Contract;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

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
