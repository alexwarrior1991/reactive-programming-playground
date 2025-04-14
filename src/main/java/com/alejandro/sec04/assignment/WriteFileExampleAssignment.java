package com.alejandro.sec04.assignment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.IntStream;

public class WriteFileExampleAssignment {

    public static void main(String[] args) {

        Path filePath = Path.of("src/main/resources/sec04/file.txt");

        try {
            // Crear directorio si no existe
            Files.createDirectories(filePath.getParent());

            // Generar líneas line1 a line1000
            Files.write(filePath,
                    (Iterable<String>) IntStream.rangeClosed(1, 1000)
                            .mapToObj(i -> "line" + i)
                            ::iterator,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Archivo generado con éxito: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
