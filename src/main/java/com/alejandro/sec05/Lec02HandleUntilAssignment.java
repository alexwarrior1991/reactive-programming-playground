package com.alejandro.sec05;

import com.alejandro.common.Util;
import reactor.core.publisher.Flux;

public class Lec02HandleUntilAssignment {

    public static void main(String[] args) {
        Flux.<String>generate(synchronousSink -> synchronousSink.next(Util.faker().country().name()))
                .handle((item, sink) -> {
                    sink.next(item);
                    if (item.equalsIgnoreCase("canada")) {
                        sink.complete();
                    }
                })
                .subscribe(Util.subscriber());
    }
}
