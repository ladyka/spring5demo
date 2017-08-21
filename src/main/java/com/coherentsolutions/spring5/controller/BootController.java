package com.coherentsolutions.spring5.controller;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Random;

@RestController
@RequestMapping("/demo")
public class BootController {

    DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

    @GetMapping("/random")
    public Mono<String> getRandom(@RequestParam String name) {
        return Mono.just(new Random().nextInt(100) + " points to " + name);
    }

    @GetMapping("/exchange")
    public Mono<Void> exchange(ServerWebExchange webExchange) {
        ServerHttpResponse response = webExchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
        DataBuffer dataBuffer = dataBufferFactory.allocateBuffer().write("Using exchange!".getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }
}
