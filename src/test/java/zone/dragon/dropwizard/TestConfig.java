package zone.dragon.dropwizard;

import io.dropwizard.core.Configuration;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Bryan Harclerode
 * @date 9/23/2016
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TestConfig extends Configuration {
    private String testProperty;
}
