package zone.dragon.dropwizard;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.glassfish.jersey.spi.Contract;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

/**
 * Factory for building {@link ConstructorInterceptor constructor interceptors} for classes and constructors annotated with {@link T}
 *
 * @param <T>
 *     Annotation type that binds this factory to a particular class or constructor
 */
@Contract
@org.jvnet.hk2.annotations.Contract
public interface AnnotatedConstructorInterceptorFactory<T extends Annotation> {

    ConstructorInterceptor provide(Constructor<?> constructor, T annotation);
}
