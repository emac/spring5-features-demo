package cn.emac.demo.spring5.reactive.repositories;

import cn.emac.demo.spring5.reactive.domain.Restaurant;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RestaurantRepository extends ReactiveCrudRepository<Restaurant, String> {

}