package cn.emac.demo.spring5.reactive;

import cn.emac.demo.spring5.reactive.domain.Restaurant;
import cn.emac.demo.spring5.reactive.repositories.RestaurantRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

/**
 * @author Emac
 * @since 2017-05-29
 */
public abstract class BaseUnitTests {

    @Autowired
    protected RestaurantRepository restaurantRepository;

    @Autowired
    protected ReactiveMongoTemplate reactiveMongoTemplate;

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

    protected abstract WebTestClient prepareClient();

    @Test
    public void testNormal() throws InterruptedException {
        // start from scratch
        restaurantRepository.deleteAll().block();

        // prepare
        WebTestClient webClient = prepareClient();
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
                .consumeWith(rs -> Flux.fromIterable(rs.getResponseBody())
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
                                    .consumeWith(r2 -> Assert.assertEquals(r1, r2.getResponseBody()));
                        })
                );
    }

    @Test
    public void testDelay() throws InterruptedException {
        // start from scratch
        restaurantRepository.deleteAll().block();

        // prepare (reset timeout to 1 minute, default value is 5 seconds)
        WebTestClient webClient = prepareClient();
        Restaurant[] restaurants = IntStream.range(0, 3)
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
                .hasSize(3);

        // findAll (1/s)
        WebTestClient.ResponseSpec exchange = webClient.get().uri("/reactive/delay/restaurants")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange();
        exchange.expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Restaurant.class)
                .hasSize(3);
    }
}
