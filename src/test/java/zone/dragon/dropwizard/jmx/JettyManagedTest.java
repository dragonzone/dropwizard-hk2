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
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import zone.dragon.dropwizard.HK2Bundle;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class JettyManagedTest {
    public static final DropwizardAppExtension<Configuration> RULE = new DropwizardAppExtension<>(JmxApp.class, new Configuration());

    public static class JmxApp extends Application<Configuration> {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            HK2Bundle.addTo(bootstrap);
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(JmxResource.class);
            environment.lifecycle().manage(new TestManaged());
        }
    }

    @Path("/jmx")
    @Singleton
    @ManagedObject
    public static class JmxResource {
        @GET
        public int getValue() {
            return 3;
        }
    }

    @ManagedObject
    public static class TestManaged implements Managed {
        @ManagedAttribute("Returns an attribute")
        public int getAttribute() {
            return 42;
        }

        @Override
        public void start() throws Exception {
        }

        @Override
        public void stop() throws Exception {
        }
    }

    protected JerseyWebTarget client = JerseyClientBuilder.createClient().target(String.format("http://localhost:%d", RULE.getLocalPort()));

    @Test
    public void testJmxAttribute()
        throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        int result = client.path("jmx").request().get(Integer.class);
        assertThat(platformMBeanServer.getAttribute(
            new ObjectName("zone.dragon.dropwizard.jmx:id=0,type=jettymanagedtest$testmanaged"),
            "attribute"
        )).isEqualTo(42);
    }
}
