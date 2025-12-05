package org.manager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.manager.dto.ProductRequestDTO;
import org.manager.dto.ProductResponseDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProductService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl = "http://localhost:8080/api/products";

    public ProductService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // =================== UTIL ===================
    private HttpRequest.Builder withAuth(HttpRequest.Builder builder, String token) {
        return builder.header("Authorization", "Bearer " + token)
                .header("Accept", "application/json");
    }

    private <T> T parseBody(String body, TypeReference<T> typeRef) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar resposta", e);
        }
    }

    private void checkStatus(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status == 403) {
            throw new RuntimeException("Acesso negado: token inválido ou sem permissão");
        }
        if (status >= 400) {
            throw new RuntimeException("Erro na requisição: HTTP " + status + " - " + response.body());
        }
    }

    // =================== CRUD ===================

    // READ ALL
    public CompletableFuture<List<ProductResponseDTO>> getAllProducts(String token) {
        HttpRequest request = withAuth(HttpRequest.newBuilder().uri(URI.create(baseUrl)), token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return response.body();
                })
                .thenApply(body -> {
                    if (body == null || body.isBlank()) {
                        return List.of();
                    }
                    return parseBody(body, new TypeReference<List<ProductResponseDTO>>() {});
                });
    }

    // READ BY ID
    public CompletableFuture<ProductResponseDTO> getProductById(Long id, String token) {
        HttpRequest request = withAuth(HttpRequest.newBuilder().uri(URI.create(baseUrl + "/" + id)), token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return parseBody(response.body(), new TypeReference<ProductResponseDTO>() {});
                });
    }

    // CREATE
    public CompletableFuture<ProductResponseDTO> createProduct(ProductRequestDTO dto, String token) {
        try {
            String requestBody = objectMapper.writeValueAsString(dto);
            HttpRequest request = withAuth(HttpRequest.newBuilder().uri(URI.create(baseUrl)), token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        checkStatus(response);
                        return parseBody(response.body(), new TypeReference<ProductResponseDTO>() {});
                    });
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar produto", e);
        }
    }

    // UPDATE
    public CompletableFuture<ProductResponseDTO> updateProduct(Long id, ProductRequestDTO dto, String token) {
        try {
            String requestBody = objectMapper.writeValueAsString(dto);
            HttpRequest request = withAuth(HttpRequest.newBuilder().uri(URI.create(baseUrl + "/" + id)), token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        checkStatus(response);
                        return parseBody(response.body(), new TypeReference<ProductResponseDTO>() {});
                    });
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar produto", e);
        }
    }

    // DELETE
    public CompletableFuture<Boolean> deleteProduct(Long id, String token) {
        HttpRequest request = withAuth(HttpRequest.newBuilder().uri(URI.create(baseUrl + "/" + id)), token)
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return response.statusCode() == 204;
                });
    }

    // =================== STATISTICS ===================

    public CompletableFuture<Long> getTotalProductsInCompany(Long companyId, String token) {
        HttpRequest request = withAuth(HttpRequest.newBuilder().uri(
                URI.create(baseUrl + "/company/" + companyId + "/total")), token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return Long.parseLong(response.body());
                });
    }

    public CompletableFuture<Long> getProductsBelowMinStock(Long companyId, String token) {
        HttpRequest request = withAuth(HttpRequest.newBuilder().uri(
                URI.create(baseUrl + "/company/" + companyId + "/below-min-stock")), token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return Long.parseLong(response.body());
                });
    }

    public CompletableFuture<Double> getTotalValueOfProducts(Long companyId, String token) {
        HttpRequest request = withAuth(HttpRequest.newBuilder().uri(
                URI.create(baseUrl + "/company/" + companyId + "/total-value")), token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return Double.parseDouble(response.body());
                });
    }

    public CompletableFuture<Map<String, Long>> getProductsByCategory(Long companyId, String token) {
        HttpRequest request = withAuth(HttpRequest.newBuilder().uri(
                URI.create(baseUrl + "/company/" + companyId + "/by-category")), token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return parseBody(response.body(), new TypeReference<Map<String, Long>>() {});
                });
    }
}
