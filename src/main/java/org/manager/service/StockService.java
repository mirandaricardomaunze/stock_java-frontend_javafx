package org.manager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.manager.dto.StockDTO;
import org.manager.dto.StockRequestDTO;
import org.manager.dto.StockResponseDTO;
import org.manager.dto.StockSummaryDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StockService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private static final String BASE_URL = "http://localhost:8080/api/stocks";

    public StockService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ============================
    // UTIL
    // ============================
    private String cleanToken(String token) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Token não pode ser nulo ou vazio");
        }
        return token.replace("Bearer ", "");
    }

    private HttpRequest.Builder withAuth(HttpRequest.Builder builder, String token) {
        return builder.header("Authorization", "Bearer " + cleanToken(token))
                .header("Accept", "application/json");
    }

    private void checkStatus(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status >= 400) {
            throw new RuntimeException("Erro HTTP " + status + ": " + response.body());
        }
    }

    private <T> T parseJson(String body, TypeReference<T> type) {
        try {
            return objectMapper.readValue(body, type);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar JSON", e);
        }
    }

    // ============================
    // CREATE / UPDATE
    // ============================
    public CompletableFuture<StockRequestDTO> createOrUpdateAsync(StockRequestDTO dto, String token) {
        try {
            String json = objectMapper.writeValueAsString(dto);

            HttpRequest request = withAuth(HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL)), token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        checkStatus(response);
                        return parseJson(response.body(), new TypeReference<StockRequestDTO>() {});
                    });

        } catch (Exception ex) {
            CompletableFuture<StockRequestDTO> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
    }

    // ============================
    // GET ALL STOCK
    // ============================
    public CompletableFuture<List<StockResponseDTO>> getAllAsync(String token) {
        HttpRequest request = withAuth(
                HttpRequest.newBuilder().uri(URI.create(BASE_URL)), token
        ).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return parseJson(response.body(), new TypeReference<List<StockResponseDTO>>() {});
                });
    }

    // ============================
    // GET STOCK BY WAREHOUSE
    // ============================
    public CompletableFuture<List<StockResponseDTO>> getAllByWarehouseAsync(Long warehouseId, String token) {
        HttpRequest request = withAuth(
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/warehouse/" + warehouseId)), token
        ).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return parseJson(response.body(), new TypeReference<List<StockResponseDTO>>() {});
                });
    }

    // ============================
    // GET STOCK BY PRODUCT
    // ============================
    public CompletableFuture<List<StockResponseDTO>> getAllByProductAsync(Long productId, String token) {
        HttpRequest request = withAuth(
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/product/" + productId)), token
        ).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return parseJson(response.body(), new TypeReference<List<StockResponseDTO>>() {});
                });
    }

    // ============================
    // TOTAL STOCK BY PRODUCT
    // ============================
    public CompletableFuture<Long> getTotalStockByProductAsync(Long productId, String token) {
        HttpRequest request = withAuth(
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/total/product/" + productId)), token
        ).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return Long.parseLong(response.body());
                });
    }

    // ============================
    // TOTAL STOCK BY WAREHOUSE
    // ============================
    public CompletableFuture<Long> getTotalStockByWarehouseAsync(Long warehouseId, String token) {
        HttpRequest request = withAuth(
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/total/warehouse/" + warehouseId)), token
        ).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return Long.parseLong(response.body());
                });
    }

    // ============================
    // SUMMARY BY PRODUCT
    // ============================
    public CompletableFuture<List<StockSummaryDTO>> getSummaryByProductAsync(String token) {
        HttpRequest request = withAuth(
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/summary/product")), token
        ).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return parseJson(response.body(), new TypeReference<List<StockSummaryDTO>>() {});
                });
    }

    // ============================
    // SUMMARY BY WAREHOUSE
    // ============================
    public CompletableFuture<List<StockSummaryDTO>> getSummaryByWarehouseAsync(String token) {
        HttpRequest request = withAuth(
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/summary/warehouse")), token
        ).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkStatus(response);
                    return parseJson(response.body(), new TypeReference<List<StockSummaryDTO>>() {});
                });
    }

    // ============================
    // DELETE STOCK
    // ============================
    public CompletableFuture<Boolean> deleteAsync(Long stockId, String token) {
        if (stockId == null) {
            CompletableFuture<Boolean> failed = new CompletableFuture<>();
            failed.completeExceptionally(new RuntimeException("ID do stock não pode ser nulo"));
            return failed;
        }

        HttpRequest request = withAuth(
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + stockId)), token
        ).DELETE().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        return true;
                    } else {
                        throw new RuntimeException("Erro ao deletar stock: HTTP " + response.statusCode());
                    }
                });
    }
}
