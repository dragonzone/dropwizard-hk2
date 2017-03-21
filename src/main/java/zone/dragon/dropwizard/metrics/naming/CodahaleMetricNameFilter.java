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
    public MetricName buildName(MetricName metricName, AnnotatedElement parent, Type metricType) {
        String parentNamespace = getParentNamespace(parent);
        String parentName      = getParentName(parent);
        String annotatedName   = getAnnotatedName(parent, metricType);
        boolean absolute = isAbsoluteName(parent, metricType);

        String name = parentName != null ? parentName : metricName.getName();
        if (annotatedName != null && !annotatedName.isEmpty()) {
            name = annotatedName;
        }
        if (!absolute && parentNamespace != null && !parentNamespace.isEmpty()) {
            name = String.format("%s.%s", parentNamespace, name);
        }
        return metricName.withName(name);
    }

    protected String getAnnotatedName(AnnotatedElement parent, Type metricType) {
        if (parent == null) {
            return null;
        }
        if (metricType == null || ReflectionHelper.getRawClass(metricType) == Timer.class) {
            Timed ann = parent.getAnnotation(Timed.class);
            if (ann != null) {
                return ann.name();
            }
        }
        if (metricType == null || ReflectionHelper.getRawClass(metricType) == Counter.class) {
            Counted ann = parent.getAnnotation(Counted.class);
            if (ann != null) {
                return ann.name();
            }
        }
        if (metricType == null || ReflectionHelper.getRawClass(metricType) == Meter.class) {
            Metered meteredAnn = parent.getAnnotation(Metered.class);
            if (meteredAnn != null) {
                return meteredAnn.name();
            }
            ExceptionMetered exceptionMeteredAnn = parent.getAnnotation(ExceptionMetered.class);
            if (exceptionMeteredAnn != null) {
                return exceptionMeteredAnn.name();
            }
        }
        if (metricType == null || ReflectionHelper.getRawClass(metricType) == com.codahale.metrics.Gauge.class) {
            Gauge gaugedAnn = parent.getAnnotation(Gauge.class);
            if (gaugedAnn != null) {
                return gaugedAnn.name();
            }
            CachedGauge cachedGaugeAnn = parent.getAnnotation(CachedGauge.class);
            if (cachedGaugeAnn != null) {
                return cachedGaugeAnn.name();
            }
        }
        Metric metricAnn = parent.getAnnotation(Metric.class);
        if (metricAnn != null) {
            return metricAnn.name();
        }
        return null;
    }

    protected boolean isAbsoluteName(AnnotatedElement parent, Type metricType) {
        if (parent == null) {
            return false;
        }
        if (metricType == null || ReflectionHelper.getRawClass(metricType) == Timer.class) {
            Timed ann = parent.getAnnotation(Timed.class);
            if (ann != null) {
                return ann.absolute();
            }
        }
        if (metricType == null || ReflectionHelper.getRawClass(metricType) == Counter.class) {
            Counted ann = parent.getAnnotation(Counted.class);
            if (ann != null) {
                return ann.absolute();
            }
        }
        if (metricType == null || ReflectionHelper.getRawClass(metricType) == Meter.class) {
            Metered meteredAnn = parent.getAnnotation(Metered.class);
            if (meteredAnn != null) {
                return meteredAnn.absolute();
            }
            ExceptionMetered exceptionMeteredAnn = parent.getAnnotation(ExceptionMetered.class);
            if (exceptionMeteredAnn != null) {
                return exceptionMeteredAnn.absolute();
            }
        }
        if (metricType == null || ReflectionHelper.getRawClass(metricType) == com.codahale.metrics.Gauge.class) {
            Gauge gaugedAnn = parent.getAnnotation(Gauge.class);
            if (gaugedAnn != null) {
                return gaugedAnn.absolute();
            }
            CachedGauge cachedGaugeAnn = parent.getAnnotation(CachedGauge.class);
            if (cachedGaugeAnn != null) {
                return cachedGaugeAnn.absolute();
            }
        }
        Metric metricAnn = parent.getAnnotation(Metric.class);
        if (metricAnn != null) {
            return metricAnn.absolute();
        }
        return false;
    }

    protected String getParentName(AnnotatedElement parent) {
        if (parent instanceof Member) {
            if (parent instanceof Constructor) {
                return ((Constructor) parent).getDeclaringClass().getSimpleName();
            }
            return ((Member) parent).getName();
        }
        if (parent instanceof Parameter) {
            return ((Parameter) parent).getName();
        }
        if (parent instanceof Type) {
            String typeName = ((Type) parent).getTypeName();
            if (typeName.contains(".")) {
                return typeName.substring(typeName.lastIndexOf('.') + 1);
            }
            return typeName;
        }
        return null;
    }

    protected String getParentNamespace(AnnotatedElement parent) {
        if (parent instanceof Member) {
            return ((Member) parent).getDeclaringClass().getCanonicalName();
        }
        if (parent instanceof Parameter) {
            Executable executable = ((Parameter) parent).getDeclaringExecutable();
            if (executable instanceof Constructor) {
                return executable.getDeclaringClass().getCanonicalName();
            }
            return String.format("%s.%s", executable.getDeclaringClass().getCanonicalName(), executable.getName());
        }
        if (parent instanceof Type) {
            String typeName = ((Type) parent).getTypeName();
            if (typeName.contains(".")) {
                return typeName.substring(0, typeName.lastIndexOf('.'));
            }
        }
        return null;
    }
}
