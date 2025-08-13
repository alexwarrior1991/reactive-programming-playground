package com.alejandro.sec03;

import com.alejandro.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class ComplexDeferExample {

    private static final Logger log = LoggerFactory.getLogger(ComplexDeferExample.class);

    public static void main(String[] args) {

        // Definimos un contador mutable que será usado en cada suscripción
        AtomicInteger counter = new AtomicInteger(0);

        // Creamos un flujo dinámico usando Flux.defer
        Flux<String> dynamicFlux = Flux.defer(() -> {
            int currentCount = counter.incrementAndGet();
            log.info("Subscriber number: {}", currentCount);
            if (currentCount % 2 == 0) {
                return Flux.interval(Duration.ofMillis(200)) // Si el contador es par, genera números infinitos
                        .map(i -> "Even Sequence: " + (i + 1)) // Se etiqueta como secuencia "par"
                        .take(5); // Emitir solo los primeros 5 elementos.
            } else {
                return Flux.just("Odd Sequence: A", "Odd Sequence: B", "Odd Sequence: C") // Contador impar
                        .delayElements(Duration.ofMillis(300)) // Retrasos entre elementos
                        .doOnComplete(() -> log.info("Odd Sequence completed")); // Operación lado-efecto.
            }
        });

        // Primera suscripción (contadores impares: 1)
        dynamicFlux.subscribe(Util.subscriber("Subscriber 1"));

        // Segunda suscripción después de 2 segundos (contadores pares: 2)
        Util.sleepSeconds(2);
        dynamicFlux.subscribe(Util.subscriber("Subscriber 2"));

        // Tercera suscripción después de 2 segundos (contadores impares nuevamente: 3)
        Util.sleepSeconds(2);
        dynamicFlux.subscribe(Util.subscriber("Subscriber 3"));

    }


}
