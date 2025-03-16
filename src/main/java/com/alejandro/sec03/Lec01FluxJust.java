package com.alejandro.sec03;

import com.alejandro.common.Util;
import reactor.core.publisher.Flux;

import java.util.List;

public class Lec01FluxJust {

    public static void main(String[] args) {

        Flux.just(1, 2, 3, "sam")
                .subscribe(Util.subscriber());
    }
}
