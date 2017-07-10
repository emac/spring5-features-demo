package cn.emac.demo.spring5.reactive.integration;

import cn.emac.demo.spring5.reactive.domain.Restaurant;
import cn.emac.demo.spring5.reactive.repositories.RestaurantRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * @author Emac
 * @since 2017-05-29
 */
public abstract class BaseIntegrationTests {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    public void testAll() throws InterruptedException {
        // start from scratch
        restaurantRepository.deleteAll().block();

        // prepare
        WebClient webClient = WebClient.create("http://localhost:9090");
        Restaurant[] restaurants = IntStream.range(0, 100)
                .mapToObj(String::valueOf)
                .map(s -> new Restaurant(s, s, s))
                .toArray(Restaurant[]::new);

        // create
        webClient.post().uri("/reactive/restaurants")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .syncBody(restaurants)
                .exchange()
                .flatMapMany(resp -> resp.bodyToFlux(Restaurant.class))
                .log()
                // 通过toStream()批量获取结果，避免死锁
                .toStream()
                .forEach(r1 -> {
                    // get
                    AtomicBoolean result = new AtomicBoolean(false);
                    CountDownLatch latch = new CountDownLatch(1);
                    webClient.get()
                            .uri("/reactive/restaurants/{id}", r1.getId())
                            .accept(MediaType.APPLICATION_JSON_UTF8)
                            .exchange()
                            .flatMap(resp -> resp.bodyToMono(Restaurant.class))
                            .log()
                            .subscribe(r2 -> result.set(r2.equals(r1)), e -> latch.countDown(), latch::countDown);
                    try {
                        latch.await();
                        Assert.assertTrue(result.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }
}
