package cn.emac.demo.spring5.reactive;

import cn.emac.demo.spring5.reactive.controllers.RestaurantController;
import cn.emac.demo.spring5.reactive.domain.Restaurant;
import cn.emac.demo.spring5.reactive.repositories.RestaurantRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.stream.IntStream;

/**
 * @author Emac
 * @since 2017-05-29
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RestaurantControllerTests2 {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Test
    public void testAll() throws InterruptedException {
        // start from scratch
        restaurantRepository.deleteAll().block();

        // prepare
        WebTestClient webClient = WebTestClient.bindToController(new RestaurantController(restaurantRepository, reactiveMongoTemplate)).build();
        Restaurant[] restaurants = IntStream.range(1, 100)
                .mapToObj(String::valueOf)
                .map(s -> new Restaurant(s, s, s))
                .toArray(Restaurant[]::new);

        // create
        webClient.post().uri("/reactive/restaurants")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .syncBody(restaurants)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Restaurant.class)
                .hasSize(99)
                .consumeWith(rs -> Flux.fromIterable(rs)
                        .log()
                        .subscribe(r1 -> {
                            // get
                            webClient.get()
                                    .uri("/reactive/restaurants/{id}", r1.getId())
                                    .accept(MediaType.APPLICATION_JSON_UTF8)
                                    .exchange()
                                    .expectStatus().isOk()
                                    .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                                    .expectBody(Restaurant.class)
                                    .consumeWith(r2 -> Assert.assertEquals(r1, r2));
                        })
                );
    }
}
