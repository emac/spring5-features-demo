package cn.emac.demo.spring5.reactive.throughtput;

import cn.emac.demo.spring5.reactive.domain.Restaurant;
import cn.emac.demo.spring5.reactive.repositories.RestaurantRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author Emac
 * @since 2017-05-29
 */
public abstract class BaseThroughtputTests {

    public static final int CONCURRENT_SIZE = 100;
    public static final int PACK_SIZE = 1_000;

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
    public void testThroughput() throws InterruptedException {
        // start from scratch
        restaurantRepository.deleteAll().block();

        // prepare (reset timeout to 1 minute, default value is 5 seconds)
        WebTestClient webClient = prepareClient();
        Restaurant[] restaurants = IntStream.range(0, PACK_SIZE)
                .mapToObj(String::valueOf)
                .map(s -> new Restaurant(s, s, s))
                .toArray(Restaurant[]::new);

        // create
        CountDownLatch latch = new CountDownLatch(CONCURRENT_SIZE);
        _runInParallel(CONCURRENT_SIZE, () -> {
            try {
                webClient.post().uri("/reactive/restaurants")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .body(Flux.just(restaurants), Restaurant.class)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                        .expectBodyList(Restaurant.class)
                        .hasSize(PACK_SIZE);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    private void _runInParallel(int nThreads, Runnable task) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            executorService.submit(task);
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
    }
}
