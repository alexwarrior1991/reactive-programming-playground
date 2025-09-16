package com.alejandro.compVthreads;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataAnalysisFlatMany {

    public static void main(String[] args) {
        AnalyticsService analyticsService = new AnalyticsService();

        // Obtener un informe y procesar todas sus métricas
        analyticsService.getReport("REPORT-2023-Q4")
                .flatMapMany(report -> {
                    System.out.println("Analizando informe: " + report.getName());
                    return Flux.fromIterable(report.getMetrics().entrySet())
                            .flatMap(entry -> analyticsService.analyzeMetric(entry.getKey(), entry.getValue()));
                })
                .subscribe(
                        analysis -> System.out.println(analysis),
                        error -> System.err.println("Error en análisis: " + error.getMessage()),
                        () -> System.out.println("Análisis completo")
                );

    }

    static class AnalyticsService {

        // Simula obtener un informe
        public Mono<Report> getReport(String reportId) {
            Map<String, Double> metrics = new ConcurrentHashMap<>();
            metrics.put("revenue", 1250000.0);
            metrics.put("expenses", 780000.0);
            metrics.put("new_customers", 1450.0);
            metrics.put("churn_rate", 0.05);

            return Mono.just(new Report(reportId, "Informe Trimestral Q4", metrics));
        }

        // Analiza una métrica específica
        public Mono<String> analyzeMetric(String metricName, Double value) {
            return Mono.just("Análisis de '" + metricName + "': " +
                    (metricName.equals("churn_rate") ?
                            (value < 0.1 ? "Saludable" : "Preocupante") :
                            "Valor: " + value));
        }
    }

    static class Report {
        private final String id;
        private final String name;
        private final Map<String, Double> metrics;

        public Report(String id, String name, Map<String, Double> metrics) {
            this.id = id;
            this.name = name;
            this.metrics = metrics;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Map<String, Double> getMetrics() {
            return metrics;
        }
    }
}
