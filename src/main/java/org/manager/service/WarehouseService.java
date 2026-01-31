package org.manager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.manager.dto.WarehouseRequestDTO;
import org.manager.dto.WarehouseResponseDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class WarehouseService {

    private static final String BASE_URL = "http://localhost:8080/warehouses";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WarehouseService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // =====================================================
    // CREATE
    // =====================================================
    public CompletableFuture<WarehouseResponseDTO> createWarehouse(
            WarehouseRequestDTO dto,
            String token) {

        try {
            String json = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return sendRequest(request, WarehouseResponseDTO.class);

        } catch (JsonProcessingException e) {
            throw new CompletionException("Erro ao serializar WarehouseRequestDTO", e);
        }
    }

    // =====================================================
    // READ
    // =====================================================

    /** 🔥 USAR NO COMBOBOX (somente ativos) */
    public CompletableFuture<List<WarehouseResponseDTO>> getActiveWarehousesByCompany(
            Long companyId,
            String token) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/company/" + companyId + "/active"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return sendListRequest(request, new TypeReference<>() {});
    }

    /** Todos os armazéns da empresa (admin / gestão) */
    public CompletableFuture<List<WarehouseResponseDTO>> getAllWarehousesByCompany(
            Long companyId,
            String token) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/company/" + companyId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return sendListRequest(request, new TypeReference<>() {});
    }

    /** Armazém principal */
    public CompletableFuture<WarehouseResponseDTO> getPrincipalWarehouseByCompany(
            Long companyId,
            String token) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/company/" + companyId + "/principal"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return sendRequest(request, WarehouseResponseDTO.class);
    }

    /** Buscar por ID */
    public CompletableFuture<WarehouseResponseDTO> getWarehouseById(
            Long id,
            String token) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return sendRequest(request, WarehouseResponseDTO.class);
    }

    // =====================================================
    // UPDATE
    // =====================================================
    public CompletableFuture<WarehouseResponseDTO> updateWarehouse(
            Long id,
            WarehouseRequestDTO dto,
            String token) {

        try {
            String json = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return sendRequest(request, WarehouseResponseDTO.class);

        } catch (JsonProcessingException e) {
            throw new CompletionException("Erro ao serializar WarehouseRequestDTO", e);
        }
    }

    // =====================================================
    // DELETE
    // =====================================================
    public CompletableFuture<Void> deleteWarehouse(Long id, String token) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 204) {
                        throw new CompletionException(
                                new RuntimeException("Erro ao deletar warehouse: " + response.body())
                        );
                    }
                });
    }

    // =====================================================
    // PRINCIPAL
    // =====================================================
    public CompletableFuture<WarehouseResponseDTO> setPrincipalWarehouse(
            Long warehouseId,
            String token) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + warehouseId + "/set-principal"))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return sendRequest(request, WarehouseResponseDTO.class);
    }

    // =====================================================
    // HELPERS
    // =====================================================
    private <T> CompletableFuture<T> sendRequest(HttpRequest request, Class<T> responseType) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        try {
                            return objectMapper.readValue(response.body(), responseType);
                        } catch (Exception e) {
                            throw new CompletionException("Erro ao converter resposta JSON", e);
                        }
                    }
                    throw new CompletionException(
                            new RuntimeException("Erro HTTP " + response.statusCode() + ": " + response.body())
                    );
                });
    }

    private <T> CompletableFuture<T> sendListRequest(HttpRequest request, TypeReference<T> typeRef) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), typeRef);
                        } catch (Exception e) {
                            throw new CompletionException("Erro ao converter lista JSON", e);
                        }
                    }
                    throw new CompletionException(
                            new RuntimeException("Erro HTTP " + response.statusCode() + ": " + response.body())
                    );
                });
    }
}
