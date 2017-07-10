package cn.emac.demo.spring5.reactive.func;

import cn.emac.demo.spring5.reactive.func.handlers.RestaurantHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.ipc.netty.http.server.HttpServer;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RouterFunctions.toHttpHandler;

/**
 * @author Emac
 * @since 2017-07-10
 */
@Configuration
public class RestaurantServer implements CommandLineRunner {

    @Autowired
    private RestaurantHandler restaurantHandler;

    @Bean
    public RouterFunction<ServerResponse> responseRouter() {
        RouterFunction<ServerResponse> router = route(GET("/reactive/restaurants").and(accept(APPLICATION_JSON_UTF8)), restaurantHandler::findAll)
                .andRoute(GET("/reactive/delay/restaurants").and(accept(APPLICATION_JSON_UTF8)), restaurantHandler::findAllDelay)
                .andRoute(GET("/reactive/restaurants/{id}").and(accept(APPLICATION_JSON_UTF8)), restaurantHandler::get)
                .andRoute(POST("/reactive/restaurants").and(accept(APPLICATION_JSON_UTF8)).and(contentType(APPLICATION_JSON_UTF8)), restaurantHandler::create)
                .andRoute(DELETE("/reactive/restaurants/{id}").and(accept(APPLICATION_JSON_UTF8)), restaurantHandler::delete)
                .filter((request, next) -> {
                    if (HttpMethod.PUT.equals(request.method())) {
                        return ServerResponse.status(HttpStatus.BAD_REQUEST).build();
                    }
                    return next.handle(request);
                });
        return router;
    }

    @Override
    public void run(String... args) throws Exception {
        RouterFunction<ServerResponse> router = responseRouter();
        HttpHandler httpHandler = toHttpHandler(router);
        ReactorHttpHandlerAdapter httpAdapter = new ReactorHttpHandlerAdapter(httpHandler);
        HttpServer server = HttpServer.create("localhost", 9090);
        server.newHandler(httpAdapter).block();
    }
}
