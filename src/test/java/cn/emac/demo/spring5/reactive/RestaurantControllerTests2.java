package cn.emac.demo.spring5.reactive;

import cn.emac.demo.spring5.reactive.controllers.RestaurantController;
import cn.emac.demo.spring5.reactive.domain.Restaurant;
import cn.emac.demo.spring5.reactive.repositories.RestaurantRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
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

    private LocalDateTime start;

    @Before
    public void before() {
        start = LocalDateTime.now();
    }

    @After
    public void after() {
        LocalDateTime end = LocalDateTime.now();
        System.out.println(String.format("\nExecution time: %s milli-seconds", Duration.between(start, end).toMillis()));
    }

    @Test
    public void testNormandy() throws InterruptedException {
        // start from scratch
        restaurantRepository.deleteAll().block();

        // prepare
        WebTestClient webClient = WebTestClient.bindToController(new RestaurantController(restaurantRepository, reactiveMongoTemplate)).build();
        Restaurant[] restaurants = IntStream.range(0, 100)
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
                .hasSize(100)
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

    @Test
    public void testDelay() throws InterruptedException {
        // start from scratch
        restaurantRepository.deleteAll().block();

        // prepare (reset timeout to 1 minute, default value is 5 seconds)
        WebTestClient webClient = WebTestClient.bindToController(new RestaurantController(restaurantRepository, reactiveMongoTemplate))
                .configureClient().responseTimeout(Duration.ofMinutes(1)).build();
        Restaurant[] restaurants = IntStream.range(0, 10)
                .mapToObj(String::valueOf)
                .map(s -> new Restaurant(s, s, s))
                .toArray(Restaurant[]::new);

        // create (1/s)
        webClient.post().uri("/reactive/restaurants")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Flux.just(restaurants).delayElements(Duration.ofSeconds(1)), Restaurant.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Restaurant.class)
                .hasSize(10);

        // findAll (1/s)
        WebTestClient.ResponseSpec exchange = webClient.get().uri("/reactive/delay/restaurants")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange();
        exchange.expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Restaurant.class)
                .hasSize(10);
    }
}
