package com.alejandro.sec04;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class SensorMonitoringSystem {

    private static final Map<String, SensorData> sensorCache = new ConcurrentHashMap<>();

    // Umbrales para alertas
    private static final double TEMP_THRESHOLD = 70.0; // Temperatura en grados °C
    private static final double VOLTAGE_THRESHOLD = 230.0; // Voltaje máximo (230V)

    public static void main(String[] args) {
        // Lista de sensores a monitorear
        String[] sensors = {"Sensor_A", "Sensor_B", "Sensor_C", "Sensor_D"};

        // Crear un flujo que monitorea todos los sensores en paralelo
        Flux.fromArray(sensors)
                .flatMap(SensorMonitoringSystem::monitorSensor, 4) // Procesar máximo 4 sensores en paralelo
                .subscribe(
                        data -> System.out.println("[INFO] Procesado: " + data),       // Cada resultado procesado
                        err -> System.err.println("[ERROR] " + err.getMessage()),       // Manejo de error
                        () -> System.out.println("[INFO] ¡Monitoreo completado!")       // Completado
                );

        // Para mantener la aplicación activa el tiempo necesario
        try {
            Thread.sleep(20000); // Simular 20 segundos de monitoreo
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Monitorea un sensor recibiendo lecturas periódicas.
     */
    private static Flux<SensorData> monitorSensor(String sensorId) {
        return Flux.interval(Duration.ofSeconds(2)) // Generar lecturas cada 2 segundos
                .flatMap(i -> getSensorData(sensorId)) // Obtener datos del sensor, ya sea desde caché o tiempo real
                .doOnNext(data -> checkThresholds(sensorId, data)) // Verificar alertas según los datos
                .take(5); // Tomar un máximo de 5 lecturas por sensor
    }

    /**
     * Obtiene datos del sensor ya sea desde caché o directamente simula una lectura en vivo.
     */
    private static Mono<SensorData> getSensorData(String sensorId) {
        return Mono.defer(() -> {
            if (sensorCache.containsKey(sensorId)) {
                System.out.println("[CACHE] Lectura obtenida desde caché para: " + sensorId);
                return Mono.just(sensorCache.get(sensorId));
            }
            System.out.println("[LIVE] Obteniendo nueva lectura para: " + sensorId);
            return fetchSensorData(sensorId)
                    .doOnNext(data -> sensorCache.put(sensorId, data)); // Actualizar caché
        });
    }

    /**
     * Simula una lectura en vivo de un sensor (tiempo real).
     */
    private static Mono<SensorData> fetchSensorData(String sensorId) {
        return Mono.fromCallable(() -> {
            Thread.sleep(1500); // Simular retraso de captura de datos
            double voltage = ThreadLocalRandom.current().nextDouble(190.0, 250.0); // Generar voltaje entre 190V y 250V
            double temperature = ThreadLocalRandom.current().nextDouble(20.0, 100.0); // Generar temperatura entre 20°C y 100°C
            return new SensorData(sensorId, voltage, temperature);
        }).subscribeOn(Schedulers.boundedElastic()); // Ejecutar en un hilo separado para evitar bloqueos
    }

    /**
     * Verifica si los datos del sensor exceden ciertos umbrales y genera alertas.
     */
    private static void checkThresholds(String sensorId, SensorData data) {
        if (data.voltage() > VOLTAGE_THRESHOLD) {
            System.out.println("⚠️ ALERTA: Voltaje alto detectado en " + sensorId +
                    " (" + data.voltage() + " V)");
        }
        if (data.temperature() > TEMP_THRESHOLD) {
            System.out.println("⚠️ ALERTA: Temperatura alta detectada en " + sensorId +
                    " (" + data.temperature() + " °C)");
        }
    }

    /**
     * Clase que representa los datos de un sensor.
     */
    static class SensorData {
        private final String sensorId;
        private final double voltage;
        private final double temperature;

        public SensorData(String sensorId, double voltage, double temperature) {
            this.sensorId = sensorId;
            this.voltage = voltage;
            this.temperature = temperature;
        }

        public String sensorId() {
            return sensorId;
        }

        public double voltage() {
            return voltage;
        }

        public double temperature() {
            return temperature;
        }

        @Override
        public String toString() {
            return "SensorData{" +
                    "sensorId='" + sensorId + '\'' +
                    ", voltage=" + voltage +
                    ", temperature=" + temperature +
                    '}';
        }
    }

}
