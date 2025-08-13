package com.alejandro.compVthreads;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class ReactivePaymentProcessor {

    private static final Random random = new Random();

    public static void main(String[] args) throws InterruptedException{
        CountDownLatch latch = new CountDownLatch(1);

        System.out.println("Iniciando procesamiento de transacciones con programación reactiva...");

        // Crear varias transacciones para procesar
        Flux.fromIterable(generateTransactions(5))
                .flatMap(transaction -> {
                    System.out.println("Procesando transacción: " + transaction.getId());

                    // Ejecutar ambos procesos en paralelo
                    Mono<ValidationResult> paymentValidation = validatePayment(transaction)
                            .subscribeOn(Schedulers.boundedElastic());

                    Mono<FraudCheckResult> fraudCheck = checkForFraud(transaction)
                            .subscribeOn(Schedulers.boundedElastic());


                    // Combinar los resultados de ambos procesos
                    return Mono.zip(paymentValidation, fraudCheck)
                            .map(tuple -> {
                                ValidationResult validationResult = tuple.getT1();
                                FraudCheckResult fraudResult = tuple.getT2();

                                // Decidir si aprobar la transacción basado en ambos resultados
                                boolean approved = validationResult.isValid() && !fraudResult.isFraudulent();
                                return finalizeTransaction(transaction, approved, validationResult.getMessage(), fraudResult.getMessage());
                            });
                })
                .doOnNext(System.out::println)
                .doOnComplete(() -> {
                    System.out.println("Procesamiento de todas las transacciones completado");
                    latch.countDown();
                })
                .subscribe();

        latch.await();
    }



    // Simula la validación de pago (Proceso 1)
    private static Mono<ValidationResult> validatePayment(Transaction transaction) {
        return Mono.fromCallable(() -> {
            System.out.println("Validando pago para transacción: " + transaction.getId());
            // Simular tiempo de procesamiento (0.5-2 segundos)
            Thread.sleep(500 + random.nextInt(1500));

            // 80% de probabilidad de éxito
            boolean isValid = random.nextDouble() < 0.8;
            String message = isValid ?
                    "Pago validado correctamente" :
                    "Fondos insuficientes o tarjeta inválida";
            return new ValidationResult(isValid, message);
        }).delayElement(Duration.ofMillis(random.nextInt(200)));
    }

    // Simula la detección de fraude (Proceso 2)
    private static Mono<FraudCheckResult> checkForFraud(Transaction transaction) {
        return Mono.fromCallable(() -> {
            System.out.println("Verificando fraude para transacción: " + transaction.getId());
            // Simular tiempo de procesamiento (1-3 segundos)
            Thread.sleep(1000 + random.nextInt(2000));

            // 90% de probabilidad de que no sea fraudulenta
            boolean isFraudulent = random.nextDouble() > 0.9;
            String message = isFraudulent ?
                    "Patrón de comportamiento sospechoso detectado" :
                    "No se detectaron indicios de fraude";

            return new FraudCheckResult(isFraudulent, message);
        }).delayElement(Duration.ofMillis(random.nextInt(300)));
    }

    // Finaliza la transacción basado en los resultados de ambos procesos
    private static TransactionResult finalizeTransaction(
            Transaction transaction, boolean approved, String validationMessage, String fraudMessage) {

        System.out.println("Finalizando transacción: " + transaction.getId() +
                " - Aprobada: " + approved);

        return new TransactionResult(
                transaction.getId(),
                transaction.getAmount(),
                approved,
                validationMessage,
                fraudMessage
        );
    }


    // Genera transacciones aleatorias para el ejemplo
    private static List<Transaction> generateTransactions(int count) {
        return Flux.range(1, count)
                .map(i -> new Transaction(
                        UUID.randomUUID().toString(),
                        50 + random.nextInt(950),
                        "user" + random.nextInt(100) + "@example.com"
                ))
                .collectList()
                .block();
    }

    // Clases de modelo

    static class Transaction {
        private final String id;
        private final double amount;
        private final String userEmail;

        public Transaction(String id, double amount, String userEmail) {
            this.id = id;
            this.amount = amount;
            this.userEmail = userEmail;
        }

        public String getId() { return id; }
        public double getAmount() { return amount; }
        public String getUserEmail() { return userEmail; }
    }

    static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }

    static class FraudCheckResult {
        private final boolean fraudulent;
        private final String message;

        public FraudCheckResult(boolean fraudulent, String message) {
            this.fraudulent = fraudulent;
            this.message = message;
        }

        public boolean isFraudulent() { return fraudulent; }
        public String getMessage() { return message; }
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
