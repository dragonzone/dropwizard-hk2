package zone.dragon.dropwizard.jmx;

import java.lang.management.ManagementFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.ClassRule;
import org.junit.Test;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import zone.dragon.dropwizard.HK2Bundle;

import static org.assertj.core.api.Assertions.assertThat;

public class MBeanActivatorTest {
    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE = new DropwizardAppRule<>(JmxApp.class, new Configuration());

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
