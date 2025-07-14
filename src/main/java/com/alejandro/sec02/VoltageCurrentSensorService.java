package com.alejandro.sec02;

import com.alejandro.common.Util;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class VoltageCurrentSensorService {

    private final Map<String, SensorData> sensorCache = new ConcurrentHashMap<>();

    // Umbrales definidos para alertas
    private static final double VOLTAGE_THRESHOLD = 220.0; // Voltaje máximo seguro
    private static final double CURRENT_THRESHOLD = 10.0;  // Corriente máxima segura

    public static void main(String[] args) {
        VoltageCurrentSensorService sensorService = new VoltageCurrentSensorService();

        // Primera consulta para un sensor (sin caché)
        sensorService.getSensorData("Sensor_A")
                .subscribe(
                        data -> System.out.println("Lectura inicial: " + data),
                        error -> System.err.println("Error: " + error.getMessage())
                );

        // Consulta posterior con caché
        Util.sleepSeconds(3);
        sensorService.getSensorData("Sensor_A")
                .subscribe(
                        data -> System.out.println("Lectura desde caché: " + data),
                        error -> System.err.println("Error: " + error.getMessage())
                );

        // Consulta para un sensor inexistente
        Util.sleepSeconds(3);
        sensorService.getSensorData("Sensor_Desconocido")
                .subscribe(
                        data -> System.out.println("Lectura: " + data),
                        error -> System.err.println("Error: " + error.getMessage())
                );

        Util.sleepSeconds(3);


    }


    public Mono<SensorData> getSensorData(String sensorId) {
        return Mono.defer(() -> {
            if (sensorCache.containsKey(sensorId)) {
                // Si los datos están en caché, los devolvemos directamente
                System.out.println("Obteniendo datos desde caché para: " + sensorId);
                return Mono.just(sensorCache.get(sensorId));
            } else {
                // Si no están en caché, realizamos la lectura del sensor
                System.out.println("Leyendo datos desde el sensor para: " + sensorId);
                return fetchSensorData(sensorId)
                        .doOnSuccess(data -> {
                           sensorCache.put(sensorId, data);
                           checkThresholds(sensorId, data);
                        });
            }
        });
    }

    /**
     * Simula la lectura de un sensor (puede fallar si el sensor no es válido).
     */
    private Mono<SensorData> fetchSensorData(String sensorId) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            try {

                System.out.println("Simulando lectura del sensor: " + sensorId);
                TimeUnit.SECONDS.sleep(2);

                // Generamos medidas simuladas (voltaje y corriente)
                double voltage = 200.0 + Math.random() * 50; // Voltaje entre 200 y 250
                double current = 5.0 + Math.random() * 7;   // Corriente entre 5 y 12
                return new SensorData(voltage, current);

            } catch (InterruptedException e) {
                throw new RuntimeException("Error al leer datos del sensor", e);
            }

        }));
    }

    /**
     * Verifica si los valores del sensor exceden límites para tomar acciones específicas.
     */
    private void checkThresholds(String sensorId, SensorData data) {
        if (data.voltage() > VOLTAGE_THRESHOLD) {
            System.out.println("⚠️ ALERTA: Voltaje alto en sensor " + sensorId + " (" + data.voltage() + " V)");
        }

        if (data.current() > CURRENT_THRESHOLD) {
            System.out.println("⚠️ ALERTA: Corriente alta en sensor " + sensorId + " (" + data.current() + " A)");
        }
    }


    public record SensorData(double voltage, double current) {
        @Override
        public String toString() {
            return "Voltaje: " + voltage + " V, Corriente: " + current + " A";
        }

    }
}
