package com.alejandro.sec02;

import com.alejandro.common.Util;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WeatherService {

    // Métodos básicos del servicio:
    public static void main(String[] args) {
        WeatherService weatherService = new WeatherService();

        // Consumo del servicio para obtener el clima por ciudad
        weatherService.getWeatherByCity("Madrid")
                .subscribe(
                        weather -> System.out.println("Resultado: " + weather),             // Resultado exitoso
                        error -> System.err.println("Error: " + error.getMessage()),        // Error
                        () -> System.out.println("¡Operación completada!")                 // Completado sin errores
                );

        Util.sleepSeconds(4);
    }

    /**
     * Simula una llamada a un servicio API que devuelve un CompletableFuture para datos del clima.
     */
    private CompletableFuture<String> fetchWeatherFromApi(String city) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simula un retraso en la API (por ejemplo, petición HTTP)
                System.out.println("Llamando al servicio de clima para: " + city + "...");
                TimeUnit.SECONDS.sleep(2);

                // Si la ciudad es "Desconocida", simulamos un error
                if ("Desconocida".equalsIgnoreCase(city)) {
                    throw new RuntimeException("Ciudad no encontrada en la base de datos.");
                }

                // Devuelve un resultado (en este caso un string representando el clima)
                return "El clima en " + city + " es soleado, 25°C";
            } catch (InterruptedException e) {
                throw new RuntimeException("Error al contactar con la API", e);
            }
        });
    }

    /**
     * Método que convierte el CompletableFuture en un Mono utilizando Mono.fromFuture.
     */
    public Mono<String> getWeatherByCity(String city) {
        return Mono.fromFuture(fetchWeatherFromApi(city))
                .doOnSubscribe(subscription -> System.out.println("Realizando la solicitud..."))
                .doOnSuccess(success -> System.out.println("Solicitud exitosa."))
                .doOnError(error -> System.err.println("Error durante la solicitud: " + error.getMessage()));
    }

}
