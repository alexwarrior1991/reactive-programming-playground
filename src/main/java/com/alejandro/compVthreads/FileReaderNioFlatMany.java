package com.alejandro.compVthreads;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReaderNioFlatMany {

    public static void main(String[] args) {
        FileService fileService = new FileService();

        // Leer un directorio y procesar cada archivo
        fileService.getDirectoryInfo("./data")
                .flatMapMany(dirInfo -> {
                    System.out.println("Procesando directorio: " + dirInfo.getPath() +
                            " con " + dirInfo.getFileCount() + " archivos");
                    return fileService.readAllFiles(dirInfo.getPath());
                })
                .subscribe(
                        fileContent -> System.out.println("Contenido: " + fileContent),
                        error -> System.err.println("Error: " + error.getMessage()),
                        () -> System.out.println("Todos los archivos procesados")
                );

        // Para mantener la aplicación en ejecución
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class FileService {
        // Obtiene información de un directorio
        public Mono<DirectoryInfo> getDirectoryInfo(String dirPath) {
            return Mono.fromCallable(() -> {
                Path path = Paths.get(dirPath);
                long fileCount = Files.list(path).count();
                return new DirectoryInfo(dirPath, fileCount);
            }).subscribeOn(Schedulers.boundedElastic());
        }

        // Lee todos los archivos en un directorio
        public Flux<String> readAllFiles(String dirPath) {
            return Flux.defer(() -> {
                try {
                    return Flux.fromStream(Files.list(Paths.get(dirPath)))
                            .filter(Files::isRegularFile)
                            .flatMap(this::readFile);
                } catch (IOException e) {
                    return Flux.error(e);
                }
            });
        }

        // Lee un archivo individual
        private Flux<String> readFile(Path filePath) {
            return Mono.fromCallable(() -> {
                        System.out.println("Leyendo archivo: " + filePath.getFileName());
                        return Files.readString(filePath, StandardCharsets.UTF_8);
                    })
                    .flatMapMany(content -> Flux.fromArray(content.split("\n")))
                    .subscribeOn(Schedulers.boundedElastic());
        }
    }

    static class DirectoryInfo {
        private final String path;
        private final long fileCount;

        public DirectoryInfo(String path, long fileCount) {
            this.path = path;
            this.fileCount = fileCount;
        }

        public String getPath() {
            return path;
        }

        public long getFileCount() {
            return fileCount;
        }
    }
}
