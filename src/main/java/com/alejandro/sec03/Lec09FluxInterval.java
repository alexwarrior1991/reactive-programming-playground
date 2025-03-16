package com.alejandro.sec03;

import com.alejandro.common.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class Lec09FluxInterval {

    public static void main(String[] args) {
        Flux.interval(Duration.ofMillis(500))
                .subscribe(Util.subscriber());

        Flux.interval(Duration.ofMillis(500))
                        .map(i -> generateSensorData())
                                .subscribe(Util.subscriber());

        Util.sleepSeconds(10);
    }

    private static String generateSensorData() {
        double temperature = ThreadLocalRandom.current().nextDouble(-10.0, 40.0); // Temperatura en °C
        double pressure = ThreadLocalRandom.current().nextDouble(900.0, 1100.0);  // Presión en hPa
        return String.format("Temperatura: %.2f °C, Presión: %.2f hPa", temperature, pressure);
    }

}
