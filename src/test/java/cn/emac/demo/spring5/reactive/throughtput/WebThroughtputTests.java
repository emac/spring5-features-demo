package cn.emac.demo.spring5.reactive.throughtput;

import cn.emac.demo.spring5.reactive.web.controllers.RestaurantController;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

/**
 * @author Emac
 * @since 2017-05-29
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class WebThroughtputTests extends BaseThroughtputTests {

    @Override
    protected WebTestClient prepareClient() {
        WebTestClient webClient = WebTestClient.bindToController(new RestaurantController(restaurantRepository, reactiveMongoTemplate))
                .configureClient().responseTimeout(Duration.ofMinutes(1)).build();
        return webClient;
    }
}
