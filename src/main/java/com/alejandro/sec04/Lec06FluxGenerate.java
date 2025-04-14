package com.alejandro.sec04;

import com.alejandro.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.Random;


public class Lec06FluxGenerate {

    private static final Logger log = LoggerFactory.getLogger(Lec06FluxGenerate.class);

    public static void main(String[] args) {


        Flux.generate(synchronousSink -> {
                    log.info("invoked");
                    synchronousSink.next(1);
//            synchronousSink.next(2);
//            synchronousSink.complete();
//                    synchronousSink.error(new RuntimeException("oops"));
                })
                .take(4)
                .subscribe(Util.subscriber());


        Random random = new Random(); // Generador de números aleatorios

        Flux.generate(() -> 0, // Estado inicial
                        (state, synchronousSink) -> {
                            int randomNumber = random.nextInt(100) + 1; // Genera un número aleatorio entre 1 y 100
                            System.out.println("Generado: " + randomNumber);
                            synchronousSink.next(randomNumber); // Emitir el valor generado

                            if (randomNumber >= 90) { // Condición para detener el flujo
                                System.out.println("¡Número alcanzado! Deteniendo el flujo.");
                                synchronousSink.complete();
                            }

                            return state + 1; // Actualizar el estado (contador)
                        })
                .subscribe(System.out::println, // Procesar cada elemento
                        Throwable::printStackTrace, // Manejar errores
                        () -> System.out.println("¡Flujo completado!")); // Mensaje al finalizar


    }


}
