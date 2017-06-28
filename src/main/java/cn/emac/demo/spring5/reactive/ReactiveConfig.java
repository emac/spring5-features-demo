package cn.emac.demo.spring5.reactive;

import cn.emac.demo.spring5.reactive.domain.Restaurant;
import cn.emac.demo.spring5.reactive.repositories.RestaurantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.core.publisher.Flux;

import java.util.stream.IntStream;

/**
 * @author Emac
 * @since 2017-05-29
 */
@Configuration
@EnableWebFlux
@EnableReactiveMongoRepositories
public class ReactiveConfig implements WebFluxConfigurer {

    @Bean
    public CommandLineRunner initData(RestaurantRepository restaurantRepository) {
        return args -> {
            restaurantRepository.deleteAll().block();
            Restaurant[] restaurants = IntStream.range(0, 3)
                    .mapToObj(String::valueOf)
                    .map(s -> new Restaurant(s, s, s))
                    .toArray(Restaurant[]::new);
            restaurantRepository.saveAll(Flux.just(restaurants)).subscribe();
        };
    }
}
