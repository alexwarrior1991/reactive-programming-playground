package com.alejandro.sec04;

import com.alejandro.common.Util;
import com.alejandro.sec04.helper.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.concurrent.*;

public class Lec03FluxSinkThreadSafety {

    private static final Logger log = LoggerFactory.getLogger(Lec03FluxSinkThreadSafety.class);


    public static void main(String[] args) {
        demo3();
    }

    private static void demo3() {
        BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

        Flux<String> eventFlux = Flux.create(fluxSink -> {
            // Hilo dedicado para consumir elementos de la cola y empujarlos al FluxSink
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    while (true) {
                        String message = messageQueue.take(); // Esperar un mensaje
                        fluxSink.next(message); // Emitir el mensaje al flujo reactivo
                        if (message.equals("END")) { // Condición para detenerse
                            fluxSink.complete();
                            break;
                        }
                    }

                } catch (InterruptedException e) {
                    fluxSink.error(e); // Manejar errores si ocurren
                }

            });
        }, FluxSink.OverflowStrategy.BUFFER);

        // Suscribirnos al Flux para procesar los eventos
        eventFlux.subscribe(
                event -> System.out.println("📥 Procesado: " + event),
                err -> System.err.println("❌ Error: " + err.getMessage()),
                () -> System.out.println("✔️ Flujo completado.")
        );

        // Simular varios productores generando eventos concurrentemente
        var executor = Executors.newFixedThreadPool(3);// Pool de 3 hilos
        for (int i = 0; i < 3; i++) {
            int threadId = i + 1;
            executor.submit(() -> produceEvents(threadId, messageQueue));
        }

        // Asegurarse de que el programa no termine antes de procesar todos los eventos
        try {
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("🚀 Aplicación terminada.");



    }

    private static void produceEvents(int producerId, BlockingQueue<String> queue) {
        try {
            for (int i = 1; i <= 5; i++) {
                String event = "Evento_" + i + "_de_Producto_" + producerId;
                queue.put(event); // Añadir evento a la cola
                System.out.println("📝 Producido: " + event);
                Thread.sleep(500); // Simular tiempo entre emisiones
            }
            // Después de producir eventos, detenerse
            if (producerId == 3) { // El último productor genera un evento "END"
                queue.put("END"); // Señal para finalizar el flujo
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void demo1() {
        var list = new ArrayList<Integer>();

        Runnable runnable = () -> {
            for (int i = 0; i < 1000; i++) {
                list.add(i);
            }
        };

        for (int i = 0; i < 10; i++) {
            Thread.ofPlatform().start(runnable);
        }
        Util.sleepSeconds(3);
        log.info("list size: {}", list.size());
    }

    private static void demo2() {
        var list = new ArrayList<String>();
        var generator = new NameGenerator();
        var flux = Flux.create(generator);
        flux.subscribe(list::add);

        Runnable runnable = () -> {
            for (int i = 0; i < 1000; i++) {
                generator.generate();
            }
        };

        for (int i = 0; i < 10; i++) {
            Thread.ofPlatform().start(runnable);
        }
        Util.sleepSeconds(3);
        log.info("list size: {}", list.size());
    }



}
