package com.alejandro.compVthreads;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualThreadExample {

    private static final ConcurrentHashMap<String, String> dataStore = new ConcurrentHashMap<>();
    private static final AtomicInteger processedCount = new AtomicInteger(0);
    private static final long startTime = System.currentTimeMillis();

    public static void main(String[] args) throws InterruptedException {
        int totalItems = 100;
        CountDownLatch latch = new CountDownLatch(totalItems);

        System.out.println("Iniciando ejemplo de virtual threads...");

        // Usar un ExecutorService basado en virtual threads
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= totalItems; i++) {
                final String id = "ID-" + i;

                executor.submit(() -> {
                    try {
                        // Obtener datos
                        String rawData = fetchDataFromSource(id);

                        // Procesar datos
                        ProcessedData processedData = processData(rawData);

                        // Almacenar resultados
                        storeResult(processedData);

                        // Actualizar contador
                        int count = processedCount.incrementAndGet();
                        if (count % 10 == 0) {
                            System.out.println("Progreso: " + count + "/" + totalItems);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }// El executor se cierra automáticamente aquí

        // Esperar a que se complete el procesamiento
        latch.await();

        long endTime = System.currentTimeMillis();
        System.out.println("Procesamiento con virtual threads completado en " + (endTime - startTime) + " ms");
        System.out.println("Total de elementos procesados: " + processedCount.get());

    }

    // Simula la obtención de datos de una fuente externa
    private static String fetchDataFromSource(String id) throws InterruptedException {
        // Simular latencia de red (100-300ms)
        Thread.sleep((long) (Math.random() * 200) + 100);
        return "Data-" + id + "-" + System.currentTimeMillis();
    }

    // Simula el procesamiento de datos
    private static ProcessedData processData(String rawData) throws InterruptedException {
        // Simular procesamiento intensivo (200-500ms)
        Thread.sleep((long) (Math.random() * 300) + 200);
        return new ProcessedData(rawData, "Processed-" + rawData);
    }

    // Simula el almacenamiento de resultados
    private static void storeResult(ProcessedData data) throws InterruptedException {
        // Simular escritura en base de datos (50-150ms)
        Thread.sleep((long) (Math.random() * 100) + 50);
        dataStore.put(data.id, data.processedValue);
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
