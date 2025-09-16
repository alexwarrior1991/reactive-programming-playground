package com.alejandro.compVthreads;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadPaymentProcessor {

    private static final Random random = new Random();

    public static void main(String[] args) throws Exception {
        System.out.println("Iniciando procesamiento de transacciones con virtual threads...");

        // Generar transacciones aleatorias
        List<Transaction> transactions = generateTransactions(5);
        List<CompletableFuture<TransactionResult>> futures = new ArrayList<>();

        // Usar un ExecutorService basado en virtual threads
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Procesar cada transacción
            transactions.stream()
                    .map(transaction -> processTransaction(transaction, executor))
                    .forEach(futures::add);

            // Esperar a que todas las transacciones se completen
            CompletableFuture<Void> allDone = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));

            // Obtener y mostrar todos los resultados
            CompletableFuture<List<TransactionResult>> allResults = allDone
                    .thenApply(v -> futures.stream()
                                    .map(CompletableFuture::join)
                                    .toList()// no bloquea por tarea hasta que allOf completó
                    );

            allResults.thenAccept(results -> {
                results.forEach(System.out::println);
                System.out.println("Procesamiento de todas las transacciones completado");
            }).join();// única espera para que el main no termine antes

        }
    }

    private static CompletableFuture<TransactionResult> processTransaction(Transaction transaction, ExecutorService executor) {

        System.out.println("Procesando transacción: " + transaction.getId());

        // Crear CompletableFuture para la validación de pago (Proceso 1)
        CompletableFuture<ValidationResult> validationFuture = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Validando pago para transacción: " + transaction.getId());
                // Simular tiempo de procesamiento (0.5-2 segundos)
                Thread.sleep(500 + random.nextInt(1500));

                // 80% de probabilidad de éxito
                boolean isValid = random.nextDouble() < 0.8;
                String message = isValid ?
                        "Pago validado correctamente" :
                        "Fondos insuficientes o tarjeta inválida";

                return new ValidationResult(isValid, message);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, executor);

        // Crear CompletableFuture para la detección de fraude (Proceso 2)
        CompletableFuture<FraudCheckResult> fraudFuture = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Verificando fraude para transacción: " + transaction.getId());
                // Simular tiempo de procesamiento (1-3 segundos)
                Thread.sleep(1000 + random.nextInt(2000));

                // 90% de probabilidad de que no sea fraudulenta
                boolean isFraudulent = random.nextDouble() > 0.9;
                String message = isFraudulent ?
                        "Patrón de comportamiento sospechoso detectado" :
                        "No se detectaron indicios de fraude";

                return new FraudCheckResult(isFraudulent, message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, executor);

        // Combinar los resultados de ambos procesos cuando ambos estén completos
        return validationFuture.thenCombine(fraudFuture, (validationResult, fraudResult) -> {
            boolean approved = validationResult.isValid() && !fraudResult.isFraudulent();

            System.out.println("Finalizando transacción: " + transaction.getId() +
                    " - Aprobada: " + approved);

            return new TransactionResult(
                    transaction.getId(),
                    transaction.getAmount(),
                    approved,
                    validationResult.getMessage(),
                    fraudResult.getMessage()
            );
        });
    }

    // Genera transacciones aleatorias para el ejemplo
    private static List<Transaction> generateTransactions(int count) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            transactions.add(new Transaction(
                    UUID.randomUUID().toString(),
                    50 + random.nextInt(950),
                    "user" + random.nextInt(100) + "@example.com"
            ));
        }
        return transactions;
    }

    // Clases de modelo (idénticas a la versión reactiva)

    static class Transaction {
        private final String id;
        private final double amount;
        private final String userEmail;

        public Transaction(String id, double amount, String userEmail) {
            this.id = id;
            this.amount = amount;
            this.userEmail = userEmail;
        }

        public String getId() {
            return id;
        }

        public double getAmount() {
            return amount;
        }

        public String getUserEmail() {
            return userEmail;
        }
    }

    static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    static class FraudCheckResult {
        private final boolean fraudulent;
        private final String message;

        public FraudCheckResult(boolean fraudulent, String message) {
            this.fraudulent = fraudulent;
            this.message = message;
        }

        public boolean isFraudulent() {
            return fraudulent;
        }

        public String getMessage() {
            return message;
        }
    }

    static class TransactionResult {
        private final String transactionId;
        private final double amount;
        private final boolean approved;
        private final String validationMessage;
        private final String fraudMessage;

        public TransactionResult(String transactionId, double amount, boolean approved,
                                 String validationMessage, String fraudMessage) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.approved = approved;
            this.validationMessage = validationMessage;
            this.fraudMessage = fraudMessage;
        }

        @Override
        public String toString() {
            return "Resultado [ID: " + transactionId +
                    ", Monto: $" + amount +
                    ", Aprobada: " + approved + "]\n" +
                    "  - Validación: " + validationMessage + "\n" +
                    "  - Fraude: " + fraudMessage;
        }
    }

}
