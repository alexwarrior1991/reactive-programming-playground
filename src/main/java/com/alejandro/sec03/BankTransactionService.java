package com.alejandro.sec03;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BankTransactionService {

    private static final Map<String, List<String>> transactionCache = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        BankTransactionService service = new BankTransactionService();

        // Primera consulta: Cliente A SIN caché
        service.getTransactions("Client_A")
                .subscribe(
                        data -> System.out.println("Transacciones Cliente A: " + data),
                        error -> System.err.println("Error: " + error.getMessage()),
                        () -> System.out.println("Consulta de Cliente A completa")
                );

        // Segunda consulta: Cliente B con datos precargados
        transactionCache.put("Client_B", List.of("Compra en tienda", "Depósito", "Transferencia"));
        service.getTransactions("Client_B")
                .subscribe(
                        data -> System.out.println("Transacciones Cliente B desde caché: " + data),
                        error -> System.err.println("Error: " + error.getMessage()),
                        () -> System.out.println("Consulta de Cliente B completa")
                );

        // Tercera consulta: Cliente A nuevamente (ahora debería usar caché)
        service.getTransactions("Client_A")
                .subscribe(
                        data -> System.out.println("Transacciones Cliente A desde caché: " + data),
                        error -> System.err.println("Error: " + error.getMessage()),
                        () -> System.out.println("Consulta de Cliente A completa")
                );

    }

    public Flux<String> getTransactions(String clientId) {
        return Flux.defer(() -> {
            if (transactionCache.containsKey(clientId)) {
                // Si las transacciones están en caché, devuélvelas directamente
                return Flux.fromIterable(transactionCache.get(clientId))
                        .doOnSubscribe(s -> System.out.println("Obteniendo transacciones desde caché para: " + clientId));
            } else {
                System.out.println("Consultando transacciones recientes para: " + clientId);
                return fetchTransactionsFromSystem(clientId)
                        .doOnNext(transactions -> transactionCache.put(clientId, transactions))
                        .flatMapMany(Flux::fromIterable);// Convertir las transacciones a un Flux
            }
        });
    }

    /**
     * Simula la consulta de transacciones en el sistema de gestión.
     */
    private Mono<List<String>> fetchTransactionsFromSystem(String clientId) {
        return Mono.fromCallable(() -> {
            // Simular una consulta con un retraso
            System.out.println("Simulando consulta al sistema de gestión para " + clientId);
            Thread.sleep(2000); // Retraso de 2 segundos
            return List.of("Compra en supermercado", "Pago de servicios", "Retiro de efectivo");
        }).delayElement(Duration.ofMillis(500));// Introducir retraso adicional para simular tiempo de red



    }

}
