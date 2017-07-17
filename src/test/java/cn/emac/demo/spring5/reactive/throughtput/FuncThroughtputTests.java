package cn.emac.demo.spring5.reactive.throughtput;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.time.Duration;

/**
 * @author Emac
 * @since 2017-07-10
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FuncThroughtputTests extends BaseThroughtputTests {

    @Autowired
    private RouterFunction<ServerResponse> restaurantRouter;

    @Override
    protected WebTestClient prepareClient() {
        WebTestClient webClient = WebTestClient.bindToRouterFunction(restaurantRouter)
                .configureClient().baseUrl("http://localhost:9090").responseTimeout(Duration.ofMinutes(1)).build();
        return webClient;
    }
}
