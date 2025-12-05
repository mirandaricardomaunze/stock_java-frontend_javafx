package org.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.manager.dto.OrderDTO;
import org.manager.dto.WarehouseResponseDTO;
import org.manager.model.Company;
import org.manager.model.Product;
import org.manager.model.Warehouse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class OrderService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl = "http://localhost:8080";

    public OrderService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ================= CREATE =================
    public CompletableFuture<OrderDTO> createOrderAsync(OrderDTO dto, String token) {
        try {
            String requestBody = objectMapper.writeValueAsString(dto);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/orders"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        System.out.println("[CREATE ORDER] Status: " + response.statusCode());
                        if (response.statusCode() != 200 && response.statusCode() != 201) {
                            throw new RuntimeException("Erro ao criar encomenda: " + response.body());
                        }
                        try {
                            return objectMapper.readValue(response.body(), OrderDTO.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Falha ao converter OrderDTO: " + e.getMessage(), e);
                        }
                    });

        } catch (Exception e) {
            CompletableFuture<OrderDTO> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    // ================= FETCH ORDERS =================
    public CompletableFuture<List<OrderDTO>> fetchOrdersAsync(String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/orders"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        if (response.statusCode() != 200) {
                            throw new RuntimeException("Erro ao buscar encomendas: " + response.statusCode());
                        }
                        String body = response.body();
                        if (body == null || body.isBlank() || body.equals("[]")) {
                            System.out.println("[FETCH ORDERS] Nenhuma encomenda encontrada.");
                            return List.of();
                        }
                        OrderDTO[] orders = objectMapper.readValue(body, OrderDTO[].class);
                        return Arrays.asList(orders);
                    } catch (Exception e) {
                        throw new RuntimeException("Falha ao processar encomendas: " + e.getMessage(), e);
                    }
                });
    }

    // ================= FETCH PRODUCTS =================
    public CompletableFuture<List<Product>> fetchProductsAsync(String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/products"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        Product[] products = objectMapper.readValue(response.body(), Product[].class);
                        return Arrays.asList(products);
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao processar produtos: " + e.getMessage(), e);
                    }
                });
    }

    // ================= FETCH COMPANIES =================
    public CompletableFuture<List<Company>> fetchCompaniesAsync(String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/companies"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        Company[] companies = objectMapper.readValue(response.body(), Company[].class);
                        return Arrays.asList(companies);
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao processar empresas: " + e.getMessage(), e);
                    }
                });
    }

    // ================= FETCH WAREHOUSES =================
    public CompletableFuture<List<Warehouse>> fetchWarehousesAsync(String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/warehouses"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        WarehouseResponseDTO[] dtos = objectMapper.readValue(response.body(), WarehouseResponseDTO[].class);
                        return Arrays.stream(dtos)
                                .map(dto -> Warehouse.builder()
                                        .id(dto.getId())
                                        .name(dto.getName())
                                        .location(dto.getLocation())
                                        .capacity(dto.getCapacity())
                                        .email(dto.getEmail())
                                        .manager(dto.getManager())
                                        .active(dto.isActive())
                                        .companyId(dto.getCompanyId())
                                        .companyName(dto.getCompanyName())
                                        .build())
                                .collect(Collectors.toList());
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao processar armazéns: " + e.getMessage(), e);
                    }
                });
    }

    // ================= EXPORT PDF =================
    public CompletableFuture<byte[]> exportOrderPdf(Long id, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/orders/" + id + "/export/pdf"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/pdf")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Erro ao exportar PDF. Status: " + response.statusCode());
                    }
                    return response.body();
                });
    }

    // ================= EXPORT EXCEL =================
    public CompletableFuture<byte[]> exportOrderExcel(Long id, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/orders/" + id + "/export/excel"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Erro ao exportar Excel. Status: " + response.statusCode());
                    }
                    return response.body();
                });
    }
}
