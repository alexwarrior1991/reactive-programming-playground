package com.alejandro.compVthreads;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigProcessorExample {

    public static void main(String[] args) {
        ConfigService configService = new ConfigService();

        // Cargar y procesar archivos de configuración
        configService.getConfigFile("./config/app-config.properties")
                .flatMapMany(configFile -> {
                    System.out.println("Procesando archivo de configuración: " + configFile.getPath());
                    return configService.parseConfigFile(configFile);
                })
                .subscribe(
                        config -> System.out.println("Configuración: " + config.getKey() + " = " + config.getValue()),
                        error -> System.err.println("Error al procesar configuración: " + error.getMessage()),
                        () -> System.out.println("Procesamiento de configuración completado")
                );
    }

    static class ConfigService {
        // Obtiene un archivo de configuración
        public Mono<ConfigFile> getConfigFile(String filePath) {
            return Mono.fromCallable(() -> {
                        Path path = Paths.get(filePath);

                        // Si el archivo no existe, crear uno de ejemplo
                        if (!Files.exists(path)) {
                            Files.createDirectories(path.getParent());
                            Files.writeString(path,
                                    "# Configuración de la aplicación\n" +
                                            "app.name=Mi Aplicación\n" +
                                            "app.version=1.0.0\n" +
                                            "db.url=jdbc:mysql://localhost:3306/mydb\n" +
                                            "db.username=admin\n" +
                                            "db.password=secret\n" +
                                            "server.port=8080\n");
                        }

                        return new ConfigFile(filePath);
                    })
                    .subscribeOn(Schedulers.boundedElastic());
        }

        // Parsea un archivo de configuración
        public Flux<ConfigEntry> parseConfigFile(ConfigFile configFile) {
            return Mono.fromCallable(() -> {
                        Path path = Paths.get(configFile.getPath());
                        return Files.lines(path)
                                .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                                .map(line -> {
                                    String[] parts = line.split("=", 2);
                                    if (parts.length == 2) {
                                        return new ConfigEntry(parts[0].trim(), parts[1].trim());
                                    } else {
                                        return new ConfigEntry(line.trim(), "");
                                    }
                                })
                                .collect(Collectors.toList());
                    })
                    .flatMapMany(configEntries -> Flux.fromIterable(configEntries))
                    .subscribeOn(Schedulers.boundedElastic());
        }

        // Aplica la configuración al sistema
        public Mono<Map<String, String>> applyConfiguration(Flux<ConfigEntry> configEntries) {
            return configEntries
                    .collectMap(ConfigEntry::getKey, ConfigEntry::getValue)
                    .map(configMap -> {
                        // Aquí se aplicaría la configuración al sistema
                        System.out.println("Aplicando " + configMap.size() + " configuraciones al sistema");
                        return configMap;
                    });
        }
    }

    static class ConfigFile {
        private final String path;

        public ConfigFile(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    static class ConfigEntry {
        private final String key;
        private final String value;

        public ConfigEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
