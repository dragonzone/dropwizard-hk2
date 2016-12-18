package zone.dragon.dropwizard;

import lombok.extern.slf4j.Slf4j;
import zone.dragon.dropwizard.lifecycle.InjectableManaged;

/**
 * @author Bryan Harclerode
 * @date 9/28/2016
 */
@Slf4j
public class TestInjectableLifeCycle implements InjectableManaged {
    @Override
    public void start() throws Exception {
        log.info("Test started");
    }

    @Override
    public void stop() throws Exception {
        log.info("Test stopped");
    }
}
