package com.alejandro.compVthreads;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DirectoryMonitoring {

    public static void main(String[] args) throws Exception{
        DirectoryMonitorService monitorService = new DirectoryMonitorService();

        // Monitorear cambios en directorios
        monitorService.getDirectoriesToMonitor()
                .flatMapIterable(directories -> {
                    System.out.println("Configurando monitoreo para " + directories.size() + " directorios");
                    return directories;
                })
                .flatMap(directory -> monitorService.monitorDirectory(directory))
                .subscribe(
                        event -> System.out.println("Evento detectado: " + event),
                        error -> System.err.println("Error en monitoreo: " + error.getMessage()),
                        () -> System.out.println("Monitoreo finalizado")
                );

        // Mantener la aplicación en ejecución
        Thread.sleep(60000); // 1 minuto
    }

    static class DirectoryMonitorService {
        // Obtiene la lista de directorios a monitorear
        public Flux<List<Path>> getDirectoriesToMonitor() {
            return Flux.just(List.of(
                    Paths.get("./logs"),
                    Paths.get("./data"),
                    Paths.get("./config")
            ));
        }

        // Monitorea un directorio específico
        public Flux<FileEvent> monitorDirectory(Path directory) {
            return Flux.defer(() -> {
                try {
                    // Asegurarse de que el directorio existe
                    if (!Files.exists(directory)) {
                        Files.createDirectories(directory);
                    }

                    System.out.println("Iniciando monitoreo de: " + directory);

                    WatchService watchService = FileSystems.getDefault().newWatchService();

                    directory.register(watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY);

                    return Flux.interval(Duration.ofMillis(500))
                            .flatMapIterable(tick -> pollEvents(watchService, directory))
                            .subscribeOn(Schedulers.boundedElastic());

                } catch (IOException e) {
                    return Flux.error(e);
                }
            }).subscribeOn(Schedulers.boundedElastic());
        }

        // Sondea eventos del WatchService
        private List<FileEvent> pollEvents(WatchService watchService, Path directory) {
            try {
                WatchKey key = watchService.poll(100, TimeUnit.SECONDS);
                if (key == null) {
                    return List.of();
                }

                List<FileEvent> events = key.pollEvents().stream()
                        .map(event -> {
                            WatchEvent.Kind<?> kind = event.kind();
                            Path fileName = (Path) event.context();
                            Path fullPath = directory.resolve(fileName);

                            String eventType;
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                eventType = "CREADO";
                            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                eventType = "ELIMINADO";
                            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                eventType = "MODIFICADO";
                            } else {
                                eventType = "DESCONOCIDO";
                            }

                            return new FileEvent(fullPath.toString(), eventType);

                        })
                        .collect(Collectors.toList());

                key.reset();
                return events;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return List.of();
            }
        }
    }

    static class FileEvent {
        private final String path;
        private final String eventType;

        public FileEvent(String path, String eventType) {
            this.path = path;
            this.eventType = eventType;
        }

        @Override
        public String toString() {
            return "Archivo " + eventType + ": " + path;
        }
    }
}
