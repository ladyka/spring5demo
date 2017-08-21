package com.coherentsolutions.spring5;

import com.coherentsolutions.spring5.domain.User;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientTest {

    @Test
    public void testGetUser() {
        WebClient client = WebClient.create("http://localhost:8765/demo5/user/");

        System.out.println(client.get()
                .uri("/swhite")
                .exchange()
                .flatMap(response -> response.bodyToMono(User.class))
                .block());
    }
}
