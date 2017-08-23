package com.coherentsolutions.spring5;


import com.coherentsolutions.spring5.domain.User;
import com.coherentsolutions.spring5.repository.ReactiveRepository;
import com.coherentsolutions.spring5.repository.ReactiveUserRepository;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class FluxTest {

    @Test
    public void empty() {
        Flux<String> flux = emptyFlux();

        StepVerifier.create(flux)
                .expectComplete()
                .verify();
    }

    public Flux<String> emptyFlux() {
        return Flux.empty();
    }


    @Test
    public void twoValuesTest() {
        Flux<String> flux = twoValuesFlux();

        StepVerifier.create(flux)
                .expectNext("foo", "bar")
                .expectComplete()
                .verify();
    }


    Flux<String> twoValuesFlux() {
        //        return Flux.just("foo", "bar");
        return Flux.fromIterable(Arrays.asList("foo", "bar"));
    }


    @Test
    public void countTest() {
        Flux<Long> flux = counter();
        StepVerifier.create(flux)
                .expectNext(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)
                .verifyComplete();
    }

    Flux<Long> counter() {
        return Flux.interval(Duration.ofMillis(100))
                .take(10);
    }


    @Test
    public void capitalizeTest() {
        Flux<User> capitalizedUsers = capitalize();
        StepVerifier.create(capitalizedUsers)
                .expectNext(
                        new User("SWHITE", "SKYLER", "WHITE"),
                        new User("JPINKMAN", "JESSE", "PINKMAN"))
                .verifyComplete();
    }

    public Flux<User> capitalize() {
        Flux<User> lowerCaseUsers = Flux.just(
                User.SKYLER,
                User.JESSE
        );
        return lowerCaseUsers
                .map(user -> new User(
                        user.getUsername().toUpperCase(),
                        user.getFirstname().toUpperCase(),
                        user.getLastname().toUpperCase())
                );
    }


    final static User MARIE = new User("mschrader", "Marie", "Schrader");
    final static User MIKE = new User("mehrmantraut", "Mike", "Ehrmantraut");

    ReactiveRepository<User> repositoryWithDelay = new ReactiveUserRepository(500);
    ReactiveRepository<User> repositoryForTwo = new ReactiveUserRepository(MARIE, MIKE);

    @Test
    public void mergeWithInterleave() {
        Flux<User> flux = mergeFluxWithInterleave(repositoryWithDelay.findAll(), repositoryForTwo.findAll());
        StepVerifier.create(flux)
                .expectNext(MARIE, MIKE, User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
                .verifyComplete();
    }

    Flux<User> mergeFluxWithInterleave(Flux<User> flux1, Flux<User> flux2) {
        return flux1.mergeWith(flux2);
    }


    @Test
    public void mergeWithNoInterleave() {
        Flux<User> flux = mergeFluxWithNoInterleave(repositoryWithDelay.findAll(), repositoryForTwo.findAll());
        StepVerifier.create(flux)
                .expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL, MARIE, MIKE)
                .verifyComplete();
    }

    Flux<User> mergeFluxWithNoInterleave(Flux<User> flux1, Flux<User> flux2) {
        return flux1.concatWith(flux2);
    }


    @Test
    public void fluxWithDoOnTest() {
        Flux<User> flux = fluxWithDoOnPrintln();
        StepVerifier.create(flux)
                .expectNextCount(4)
                .verifyComplete();
    }

    Flux<User> fluxWithDoOnPrintln() {
        return new ReactiveUserRepository().findAll()
                .doOnSubscribe(subscription -> System.out.println("Starting: "))
                .doOnNext(userSignal -> {
                    System.out.println("firstname lastname: " + userSignal.getFirstname() + " " + userSignal.getLastname());
                })
                .doOnComplete(() -> System.out.println("The end!"));
    }

    @Test
    public void getValueTest() {
        System.out.println(getValue(Mono.just("foo")));
    }

    String getValue(Mono<String> mono) {
        return mono.block();
    }

    @Test
    public void getValuesTest() {
        System.out.println(getValues(Flux.just("foo", "bar")));
    }

    List<String> getValues(Flux<String> flux) {
//        flux.toIterable()
        return flux.toStream().collect(Collectors.toList());
    }


    Flux<String> asyncStringLookup(List<String> list) {
        return Flux.defer(() -> Flux.fromIterable(list)) // path repository with data loading
            .subscribeOn(Schedulers.elastic());
    }

    Mono<Void> asyncWriteStrings(Flux<String> flux) {
        return flux.publishOn(Schedulers.parallel())
            .doOnNext(System.out::println) // e.g. save to repository
            .then();
    }

    // more samples and playground: https://github.com/reactor/lite-rx-api-hands-on

}
