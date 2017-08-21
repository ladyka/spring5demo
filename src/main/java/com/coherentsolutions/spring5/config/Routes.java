package com.coherentsolutions.spring5.config;

import com.coherentsolutions.spring5.domain.User;
import com.coherentsolutions.spring5.repository.ReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Random;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class Routes {

    private final ReactiveRepository reactiveRepository;

    @Autowired
    public Routes(ReactiveRepository reactiveRepository) {
        this.reactiveRepository = reactiveRepository;
    }

    @Bean
    public RouterFunction<?> routerFunction() {
        return nest(path("/demo5"),
                route(GET("/random"), this::random)
                        .andRoute(GET("/hello/{name}"), this::hello)
                        .filter(this::logFilter)
                        .andRoute(GET("/user/{username}"), this::getUser)
        );
    }

    private Mono<ServerResponse> getUser(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .body(reactiveRepository.findById(serverRequest.pathVariable("username")),
                        User.class);
    }


    private Mono<ServerResponse> random(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .body(Mono.just(new Random().nextInt(100) + " points to " +
                                serverRequest.queryParam("name").orElse("")),
                        String.class);
    }

    private Mono<ServerResponse> hello(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .body(Mono.just("Hello, " +
                                serverRequest.pathVariable("name")),
                        String.class);
    }

    private Mono<ServerResponse> logFilter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        System.out.println("Before handler invocation: " + request.path());
        Mono<ServerResponse> responseMono = next.handle(request);
        responseMono.subscribe(serverResponse -> System.out.println("After handler invocation: " + serverResponse.statusCode()));
        return responseMono;
    }
}
