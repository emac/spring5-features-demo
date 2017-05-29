package cn.emac.demo.spring5.reactive;

import cn.emac.demo.spring5.reactive.domain.Restaurant;
import cn.emac.demo.spring5.reactive.repositories.RestaurantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author Emac
 * @since 2017-05-29
 */
@Configuration
@EnableWebFlux
@EnableReactiveMongoRepositories
public class ReactiveConfig {

    @Bean
    public CommandLineRunner initData(RestaurantRepository restaurantRepository) {
        return args -> {
            restaurantRepository.deleteAll().block();
            restaurantRepository.save(new Restaurant("hello", "hello", "hello")).block();
        };
    }
}
