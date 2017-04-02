package zone.dragon.dropwizard.metrics.naming;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.CachedGauge;
import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Metric;
import com.codahale.metrics.annotation.Timed;
import lombok.Value;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

import javax.inject.Singleton;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 * Extracts names from the codahale metric annotations ({@link Gauge @Guage}, {@link CachedGauge @CachedGauge}, {@link Timed @Timed},
 * {@link Metered @Metered}, {@link ExceptionMetered @ExceptionMetered}, and {@link Metric @Metric})
 */
@Singleton
@Rank(MetricNameFilter.DEFAULT_NAME_PRIORITY)
public class CodahaleMetricNameFilter implements MetricNameFilter {
    @Override
    public MetricName buildName(MetricName metricName, AnnotatedElement injectionSite, Type metricType) {
        // only change if it's not already been set
        if (metricName.getName() != null) {
            return metricName;
        }
        String              injecteeNamespace = getNamespace(injectionSite);
        String              injecteeName      = getName(injectionSite);
        AnnotatedMetricInfo annotation      = getAnnotation(injectionSite, metricType);
        boolean             absoluteName    = annotation != null && annotation.isAbsolute();

        String name = injecteeName != null ? injecteeName : metricName.getName();
        if (annotation != null && annotation.getName() != null && !annotation.getName().isEmpty()) {
            name = annotation.getName();
        }
        if (!absoluteName && injecteeNamespace != null && !injecteeNamespace.isEmpty()) {
            name = String.format("%s.%s", injecteeNamespace, name);
        }
        return metricName.setName(name);
    }

    /**
     * Extracts name information from codahale annotations on the {@code injectionSite}
     *
     * @param injectionSite
     *     Injection site into which the metric is being injected
     * @param metricType
     *     Type of metric being injected
     *
     * @return The name information for this injection site, and whether the name provided includes a namespace or not
     */
    protected AnnotatedMetricInfo getAnnotation(AnnotatedElement injectionSite, Type metricType) {
        if (injectionSite == null) {
            return null;
        }
        if (metricType == null || ReflectionHelper.getRawClass(metricType) == Timer.class) {
            Timed ann = injectionSite.getAnnotation(Timed.class);
            if (ann != null) {
                return AnnotatedMetricInfo.of(ann.name(), ann.absolute());
            }
        }
        if (metricType == null || ReflectionHelper.getRawClass(metricType) == Counter.class) {
            Counted ann = injectionSite.getAnnotation(Counted.class);
            if (ann != null) {
                return AnnotatedMetricInfo.of(ann.name(), ann.absolute());
            }
        }
        if (metricType == null || ReflectionHelper.getRawClass(metricType) == Meter.class) {
            Metered meteredAnn = injectionSite.getAnnotation(Metered.class);
            if (meteredAnn != null) {
                return AnnotatedMetricInfo.of(meteredAnn.name(), meteredAnn.absolute());
            }
            ExceptionMetered exceptionMeteredAnn = injectionSite.getAnnotation(ExceptionMetered.class);
            if (exceptionMeteredAnn != null) {
                if (exceptionMeteredAnn.name().isEmpty()) {
                    return AnnotatedMetricInfo.of(
                        String.format("%s.%s", getName(injectionSite), ExceptionMetered.DEFAULT_NAME_SUFFIX),
                        exceptionMeteredAnn.absolute()
                    );
                }
                return AnnotatedMetricInfo.of(exceptionMeteredAnn.name(), exceptionMeteredAnn.absolute());
            }
        }
        if (metricType == null || ReflectionHelper.getRawClass(metricType) == com.codahale.metrics.Gauge.class) {
            Gauge gaugedAnn = injectionSite.getAnnotation(Gauge.class);
            if (gaugedAnn != null) {
                return AnnotatedMetricInfo.of(gaugedAnn.name(), gaugedAnn.absolute());
            }
            CachedGauge cachedGaugeAnn = injectionSite.getAnnotation(CachedGauge.class);
            if (cachedGaugeAnn != null) {
                return AnnotatedMetricInfo.of(cachedGaugeAnn.name(), cachedGaugeAnn.absolute());
            }
        }
        Metric metricAnn = injectionSite.getAnnotation(Metric.class);
        if (metricAnn != null) {
            return AnnotatedMetricInfo.of(metricAnn.name(), metricAnn.absolute());
        }
        return null;
    }

    /**
     * Returns the name of the injection site; Usually this is the name of the element, except for constructors, which use the class name
     *
     * @param injectionSite
     *     Injection site into which the metric is being injected
     *
     * @return The name of the injection site, or {@code null} if it cannot be determined
     */
    protected String getName(AnnotatedElement injectionSite) {
        if (injectionSite instanceof Member) {
            if (injectionSite instanceof Constructor) {
                return ((Constructor) injectionSite).getDeclaringClass().getSimpleName();
            }
            return ((Member) injectionSite).getName();
        }
        if (injectionSite instanceof Parameter) {
            return ((Parameter) injectionSite).getName();
        }
        if (injectionSite instanceof Type) {
            String typeName = ((Type) injectionSite).getTypeName();
            if (typeName.contains(".")) {
                return typeName.substring(typeName.lastIndexOf('.') + 1);
            }
            return typeName;
        }
        return null;
    }

    /**
     * Returns the namespace of the injection site; Usually this is the name of the declaring class, except for top-level types, which
     * use the package name.
     *
     * @param injectionSite
     *     Injection site into which the metric is being injected
     *
     * @return The namespace of the injection site, or {@code null} if it cannot be determined
     */
    protected String getNamespace(AnnotatedElement injectionSite) {
        if (injectionSite instanceof Member) {
            return ((Member) injectionSite).getDeclaringClass().getCanonicalName();
        }
        if (injectionSite instanceof Parameter) {
            Executable executable = ((Parameter) injectionSite).getDeclaringExecutable();
            if (executable instanceof Constructor) {
                return executable.getDeclaringClass().getCanonicalName();
            }
            return String.format("%s.%s", executable.getDeclaringClass().getCanonicalName(), executable.getName());
        }
        if (injectionSite instanceof Type) {
            String typeName = ((Type) injectionSite).getTypeName();
            if (typeName.contains(".")) {
                return typeName.substring(0, typeName.lastIndexOf('.'));
            }
        }
        return null;
    }

    @Value(staticConstructor = "of")
    protected static class AnnotatedMetricInfo {
        String  name;
        boolean absolute;
    }
}
