package zone.dragon.dropwizard.health;

import com.codahale.metrics.health.HealthCheck;
import org.glassfish.jersey.spi.Contract;

/**
 * @author Darth Android
 * @date 9/23/2016
 */
@Contract
public abstract class InjectableHealthCheck extends HealthCheck {}
