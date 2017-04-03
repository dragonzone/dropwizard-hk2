package zone.dragon.dropwizard.jmx;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.ClassRule;
import org.junit.Test;
import zone.dragon.dropwizard.HK2Bundle;

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
import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Darth Android
 * @date 4/2/2017
 */
public class JettyManagedTest {
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
        int         result              = client.path("jmx").request().get(Integer.class);
        assertThat(platformMBeanServer.getAttribute(new ObjectName("zone.dragon.dropwizard.jmx:id=0,type=jettymanagedtest$testmanaged"),
                                                    "attribute"
        )).isEqualTo(42);
    }
}
