package com.alejandro.sec05;

/*
* 1. **Combina lógica de filtrado y transformación**: Puedes usar `handle` para procesar cada elemento de un flujo y decidir:
    - Transformar el valor.
    - Filtrar el valor si no cumple un criterio.

2. **Uso de un `BiConsumer`**: Toma un `BiConsumer` como argumento, lo que significa que puedes realizar operaciones sobre cada elemento del flujo y controlar la salida del mismo. Si decides no emitir un valor dentro del `BiConsumer`, ese elemento será filtrado automáticamente.
3. **Flexible y expresivo**: Proporciona flexibilidad al combinar múltiples lógicas dentro de un único operador. Esto lo convierte en una herramienta útil cuando las operaciones de transformación y filtrado están entrelazadas.

* */

import com.alejandro.common.Util;
import reactor.core.publisher.Flux;

public class Lec01Handle {

    public static void main(String[] args) {
        Flux<Integer> flux = Flux.range(1, 10);

        flux.handle((item, sink) -> {
                    sink.error(new RuntimeException("oops"));
                })
                .subscribe(Util.subscriber());

        Flux.range(1, 10)
                .filter(i -> i != 7)
                .handle((integer, synchronousSink) -> {
                    switch (integer) {
                        case 1 -> synchronousSink.next(-2);
                        case 4 -> {}
                        case 7 -> synchronousSink.error(new RuntimeException("oops"));
                        default -> synchronousSink.next(integer);
                    }
                })
                .cast(Integer.class)
                .subscribe(Util.subscriber());
    }
}
