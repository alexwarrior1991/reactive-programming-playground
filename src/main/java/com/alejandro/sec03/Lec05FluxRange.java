package com.alejandro.sec03;

import com.alejandro.common.Util;
import reactor.core.publisher.Flux;

public class Lec05FluxRange {

    public static void main(String[] args) {

        Flux.range(3, 10)
                .subscribe(Util.subscriber());

        Flux.range(1, 10)
                .map(i -> Util.faker().name().firstName())
                .subscribe(Util.subscriber());
    }
}
