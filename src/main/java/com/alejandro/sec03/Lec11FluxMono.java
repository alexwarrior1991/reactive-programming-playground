package com.alejandro.sec03;

import com.alejandro.common.Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Lec11FluxMono {

    public static void main(String[] args) {

        monoToFlux();

        //Flux to mono

        var flux = Flux.range(1, 10);

        flux.next()
                .subscribe(Util.subscriber());

        Mono.from(flux)
                .subscribe(Util.subscriber());
    }

    private static void monoToFlux(){
        var mono = getUsername(1);
        save(Flux.from(mono));
    }

    private static Mono<String> getUsername(int userId) {
        return switch (userId) {
            case 1 -> Mono.just("sam");
            case 2 -> Mono.empty(); //null
            default -> Mono.error(new IllegalArgumentException("invalid input"));
        };
    }

    private static void save(Flux<String> flux) {
        flux.subscribe(Util.subscriber());
    }
}
