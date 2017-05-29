package cn.emac.demo.spring5.reactive.controllers;

import cn.emac.demo.spring5.reactive.domain.Restaurant;
import cn.emac.demo.spring5.reactive.repositories.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author Emac
 * @since 2017-05-29
 */
@RestController
public class RestaurantController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @GetMapping("/reactive/restaurant/{id}")
    public Mono<Restaurant> get(@PathVariable String id) {
        return restaurantRepository.findById(id);
    }

    @PostMapping("/reactive/restaurant")
    public Flux<Restaurant> create(@RequestBody Restaurant[] restaurants) {
        return Flux.just(restaurants)
                .log()
                .flatMap(r -> Mono.just(r).subscribeOn(Schedulers.parallel()), 10)
                .flatMap(reactiveMongoTemplate::insert);
    }
}
