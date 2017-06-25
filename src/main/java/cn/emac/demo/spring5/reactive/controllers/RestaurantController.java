package cn.emac.demo.spring5.reactive.controllers;

import cn.emac.demo.spring5.reactive.domain.Restaurant;
import cn.emac.demo.spring5.reactive.repositories.RestaurantRepository;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * @author Emac
 * @since 2017-05-29
 */
@RestController
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public RestaurantController(RestaurantRepository restaurantRepository, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.restaurantRepository = restaurantRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @GetMapping("/reactive/restaurants")
    public Flux<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    @GetMapping("/reactive/delay/restaurants")
    public Flux<Restaurant> findAllDelay() {
        return restaurantRepository.findAll().delayElements(Duration.ofSeconds(1));
    }

    @GetMapping("/reactive/restaurants/{id}")
    public Mono<Restaurant> get(@PathVariable String id) {
        return restaurantRepository.findById(id);
    }

    @PostMapping("/reactive/restaurants")
    public Flux<Restaurant> create(@RequestBody Flux<Restaurant> restaurants) {
        return restaurants
                .log()
                .flatMap(r -> Mono.just(r).subscribeOn(Schedulers.parallel()), 10)
                .flatMap(reactiveMongoTemplate::insert);
    }
}
