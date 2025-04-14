package com.alejandro.sec04;

import com.alejandro.common.Util;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

public class Lec08GenerateWithState {

    public static void main(String[] args) {

        Flux.generate(
                        () -> 0,
                        (counter, sink) -> {
                            var country = Util.faker().country().name();
                            sink.next(country);
                            counter++;
                            if (counter == 10 || country.equalsIgnoreCase("canada")) {
                                sink.complete();
                            }
                            return counter++;
                        }
                )
                .subscribe(Util.subscriber());
    }

    private static void extracted() {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Flux.generate(synchronousSink -> {
                    var country = Util.faker().country().name();
                    synchronousSink.next(country);
                    atomicInteger.incrementAndGet();
                    if (atomicInteger.get() == 10 || country.equalsIgnoreCase("canada")) {
                        synchronousSink.complete();
                    }
                })
                .subscribe(Util.subscriber());
    }
}
