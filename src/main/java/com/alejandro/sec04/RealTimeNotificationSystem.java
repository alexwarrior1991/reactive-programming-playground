package com.alejandro.sec04;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class RealTimeNotificationSystem {

    public static void main(String[] args) {

        // Crear un pool de ejecución para manejar múltiples clientes concurrentes
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // Crear un FLUX utilizando FluxSink para emitir mensajes en tiempo real
        Flux<String> userRequestFlux = Flux.create(fluxSink -> {
            for (int i = 1; i <= 10; i++) {
                int userId = i;
                executorService.submit(() -> generateUserRequest(userId, fluxSink));
            }
        }, FluxSink.OverflowStrategy.BUFFER);

        userRequestFlux
                .delayElements(Duration.ofMillis(500))
                .subscribe(
                        message -> System.out.println("📬 Mensaje recibido: " + message),
                        err -> System.err.println("❌ Error procesando mensaje: " + err.getMessage()),
                        () -> System.out.println("✔️ Procesamiento completado")

                );

        // Mantener la ejecución activa para que todos los mensajes puedan ser enviados
        try {
            Thread.sleep(10000); // Simular tiempo suficiente para procesamiento
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executorService.shutdown();


    }

    /**
     * Método para generar solicitudes por usuario y enviarlas al FluxSink.
     */
    private static void generateUserRequest(int userId, FluxSink<String> fluxSink) {
        try {
            String request = "Solicitud del Usuario_" + userId;

            // Simular un retraso aleatorio en la creación de solicitudes
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 1000));

            if (ThreadLocalRandom.current().nextBoolean()) {
                // Emitir una solicitud correcta
                fluxSink.next(request + " procesada correctamente ✅");
            } else {
                // Simular un error en la solicitud y manejarlo
                fluxSink.next(request + " falló ❌ (error de validación)");
            }

        } catch (InterruptedException e) {
            fluxSink.error(new RuntimeException("Error al generar solicitud del Usuario_" + userId));
        }
    }

}
