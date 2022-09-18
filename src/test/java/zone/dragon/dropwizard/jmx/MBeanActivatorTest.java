package zone.dragon.dropwizard.jmx;

import java.lang.management.ManagementFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import zone.dragon.dropwizard.HK2Bundle;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class MBeanActivatorTest {
    public static final DropwizardAppExtension<Configuration> RULE = new DropwizardAppExtension<>(JmxApp.class, new Configuration());

    public static class JmxApp extends Application<Configuration> {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            HK2Bundle.addTo(bootstrap);
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(JmxResource.class);
            environment.jersey().register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(ExposedObject.class).to(Exposed.class).in(Singleton.class);
                }
            });
        }
    }

    @Path("/jmx")
    @Singleton
    @ManagedObject
    public static class JmxResource {

        @Inject
        public JmxResource(Exposed instance) {
            instance.getOtherAttribute();
        }

        @ManagedAttribute("Returns an attribute")
        public int getAttribute() {
            return 42;
        }

        @GET
        public int getValue() {
            return 3;
        }
    }

    public interface Exposed {
        int getOtherAttribute();
    }

    @ManagedObject
    public static class ExposedObject implements Exposed {
        @ManagedAttribute("Returns an attribute")
        @Timed
        public int getManagedAttribute() {
            return 24;
        }

        public int getOtherAttribute() {
            return 6;
        }
    }

    protected JerseyWebTarget client = JerseyClientBuilder.createClient().target(String.format("http://localhost:%d", RULE.getLocalPort()));

    @Test
    public void testJmxAttribute()
    throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        int         result              = client.path("jmx").request().get(Integer.class);
        assertThat(platformMBeanServer.getAttribute(new ObjectName("zone.dragon.dropwizard.jmx:id=0,type=mbeanactivatortest$jmxresource"),
                                                    "attribute"
        )).isEqualTo(42);
        ObjectName name = platformMBeanServer
            .queryNames(new ObjectName("zone.dragon.dropwizard.jmx:id=0," + "type=mbeanactivatortest$exposedobject*"), null)
            .iterator()
            .next();
        assertThat(platformMBeanServer.getAttribute(name, "managedAttribute")).isEqualTo(24);
    }
}
