package cn.emac.demo.spring5.reactive.func.handlers;

import cn.emac.demo.spring5.reactive.domain.Restaurant;
import cn.emac.demo.spring5.reactive.repositories.RestaurantRepository;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author Emac
 * @since 2017-07-10
 */
@Component
public class RestaurantHandler {

    /**
     * 扩展ReactiveCrudRepository接口，提供基本的CRUD操作
     */
    private final RestaurantRepository restaurantRepository;

    /**
     * spring-boot-starter-data-mongodb-reactive提供的通用模板
     */
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public RestaurantHandler(RestaurantRepository restaurantRepository, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.restaurantRepository = restaurantRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        Flux<Restaurant> result = restaurantRepository.findAll();
        return ok().contentType(APPLICATION_JSON_UTF8).body(result, Restaurant.class);
    }

    public Mono<ServerResponse> findAllDelay(ServerRequest request) {
        Flux<Restaurant> result = restaurantRepository.findAll().delayElements(Duration.ofSeconds(1));
        return ok().contentType(APPLICATION_JSON_UTF8).body(result, Restaurant.class);
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Restaurant> result = restaurantRepository.findById(id);
        return ok().contentType(APPLICATION_JSON_UTF8).body(result, Restaurant.class);
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Flux<Restaurant> restaurants = request.bodyToFlux(Restaurant.class);
        Flux<Restaurant> result = restaurants
                .buffer(10000)
                .flatMap(rs -> reactiveMongoTemplate.insert(rs, Restaurant.class));
        return ok().contentType(APPLICATION_JSON_UTF8).body(result, Restaurant.class);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Void> result = restaurantRepository.deleteById(id);
        return ok().contentType(APPLICATION_JSON_UTF8).build(result);
    }
}
