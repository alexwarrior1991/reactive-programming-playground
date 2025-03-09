package com.alejandro.sec02;

import com.alejandro.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class Lec04MonoEmptyError {

    private static final Logger log = LoggerFactory.getLogger(Lec04MonoEmptyError.class);

    public static void main(String[] args) {

        getUsername(3)
                .subscribe(Util.subscriber());

        getUsername(3)
                .subscribe(
                        s -> System.out.println(s),
                        err -> {}
                );

    }

    private static Mono<String> getUsername(int userId) {
        return switch (userId) {
            case 1 -> Mono.just("sam");
            case 2 -> Mono.empty(); //null
            default -> Mono.error(new IllegalArgumentException("invalid input"));
        };
    }
}
