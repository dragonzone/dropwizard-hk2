package zone.dragon.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Timed;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import zone.dragon.dropwizard.AnnotatedMethodInterceptorFactory;
import zone.dragon.dropwizard.AnnotationInterceptionService;
import zone.dragon.dropwizard.metrics.interceptors.CountedMethodInterceptorFactory;
import zone.dragon.dropwizard.metrics.interceptors.TimedMethodInterceptorFactory;
import zone.dragon.dropwizard.metrics.naming.CodahaleMetricNameFilter;
import zone.dragon.dropwizard.metrics.naming.DefaultMetricNameFormatter;
import zone.dragon.dropwizard.metrics.naming.DefaultMetricNameService;
import zone.dragon.dropwizard.metrics.naming.MetricNameFilter;
import zone.dragon.dropwizard.metrics.naming.MetricNameFormatter;
import zone.dragon.dropwizard.metrics.naming.MetricNameService;

import javax.inject.Singleton;

/**
 * Binder that registers all of the metric components with HK2; It is expected that the {@link MetricRegistry} and
 * {@link AnnotationInterceptionService} are already bound in HK2.
 */
public class HK2MetricBinder extends AbstractBinder {
    @Override
    protected void configure() {
        // Binding naming services
        bind(DefaultMetricNameService.class).to(MetricNameService.class).in(Singleton.class);
        bind(DefaultMetricNameFormatter.class).to(MetricNameFormatter.class).in(Singleton.class);
        bind(CodahaleMetricNameFilter.class).to(MetricNameFilter.class).in(Singleton.class).ranked(MetricNameFilter.DEFAULT_NAME_PRIORITY);
        // Bind Metric method handlers
        bind(GaugeAnnotationActivator.class).to(InstanceLifecycleListener.class).in(Singleton.class);
        bind(CachedGaugeAnnotationActivator.class).to(InstanceLifecycleListener.class).in(Singleton.class);
        bind(TimedMethodInterceptorFactory.class).to(new TypeLiteral<AnnotatedMethodInterceptorFactory<Timed>>() {});
        bind(CountedMethodInterceptorFactory.class).to(new TypeLiteral<AnnotatedMethodInterceptorFactory<Counted>>() {});
    }
}
