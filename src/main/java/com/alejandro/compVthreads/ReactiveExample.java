package com.alejandro.compVthreads;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ReactiveExample {

    private static final ConcurrentHashMap<String, String> dataStore = new ConcurrentHashMap<>();
    private static final AtomicInteger processedCount = new AtomicInteger(0);
    private static final long startTime = System.currentTimeMillis();

    public static void main(String[] args) throws InterruptedException {
        int totalItems = 100;
        CountDownLatch latch = new CountDownLatch(1);

        System.out.println("Iniciando ejemplo de programación reactiva...");

        // Crear un flujo de IDs de datos
        Flux.range(1, totalItems)
                .flatMap(id -> {
                    // Simular obtención de datos de múltiples fuentes en paralelo
                    return fetchDataFromSource("ID-" + id)
                            .subscribeOn(Schedulers.boundedElastic());// Usar hilos del pool elástico
                })
                .flatMap(data -> {
                    // Procesar los datos (operación que consume tiempo)
                    return processData(data)
                            .subscribeOn(Schedulers.parallel());
                })
                .flatMap(processedData -> {
                    // Almacenar los resultados
                    return storeResult(processedData);
                })
                .doOnNext(id -> {
                    int count = processedCount.incrementAndGet();
                    if (count % 10 == 0) {
                        System.out.println("Progreso: " + count + "/" + totalItems);
                    }
                })
                .doOnComplete(() -> {
                    long endTime = System.currentTimeMillis();
                    System.out.println("Procesamiento reactivo completado en " + (endTime - startTime) + " ms");
                    System.out.println("Total de elementos procesados: " + processedCount.get());
                    latch.countDown();
                })
                .subscribe();


        latch.await();
    }

    // Simula la obtención de datos de una fuente externa
    private static Mono<String> fetchDataFromSource(String id) {
        return Mono.fromCallable(() -> {
            // Simular latencia de red (100-300ms)
            Thread.sleep((long) (Math.random() * 200) + 100);
            return "Data-" + id + "-" + System.currentTimeMillis();
        });
    }

    // Simula el procesamiento de datos
    private static Mono<ProcessedData> processData(String rawData) {
        return Mono.fromCallable(() -> {
            // Simular procesamiento intensivo (200-500ms)
            Thread.sleep((long) (Math.random() * 300) + 200);
            return new ProcessedData(rawData, "Processed-" + rawData);
        });
    }

    // Simula el almacenamiento de resultados
    private static Mono<String> storeResult(ProcessedData data) {
        return Mono.fromCallable(() -> {
            // Simular escritura en base de datos (50-150ms)
            Thread.sleep((long) (Math.random() * 100) + 50);
            dataStore.put(data.id, data.processedValue);
            return data.id;
        });
    }

    // Clase para representar datos procesados
    static class ProcessedData {
        final String id;
        final String processedValue;

        ProcessedData(String id, String processedValue) {
            this.id = id;
            this.processedValue = processedValue;
        }
    }
}
