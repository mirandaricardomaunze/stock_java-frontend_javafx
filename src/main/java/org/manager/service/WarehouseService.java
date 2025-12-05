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

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String BASE_URL = "http://localhost:8080/warehouses";

    public WarehouseService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // ===== CREATE =====
    public CompletableFuture<WarehouseResponseDTO> createWarehouse(WarehouseRequestDTO warehouseDTO, String token) {
        try {
            String requestBody = objectMapper.writeValueAsString(warehouseDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            try {
                                return objectMapper.readValue(response.body(), WarehouseResponseDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new CompletionException("Erro ao converter resposta JSON", e);
                            }
                        } else {
                            throw new CompletionException("Erro ao criar armazém: " + response.statusCode() + " - " + response.body(), null);
                        }
                    });

        } catch (JsonProcessingException e) {
            throw new CompletionException("Erro ao serializar WarehouseRequestDTO para JSON", e);
        }
    }

    // ===== READ ALL =====
    public CompletableFuture<List<WarehouseResponseDTO>> getAllWarehouses(String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        try {
                            return objectMapper.readValue(response.body(), new TypeReference<List<WarehouseResponseDTO>>() {});
                        } catch (JsonProcessingException e) {
                            throw new CompletionException("Erro ao converter JSON em List<WarehouseResponseDTO>", e);
                        }
                    } else {
                        throw new CompletionException("Erro HTTP ao listar armazéns: " + response.statusCode(), null);
                    }
                });
    }

    // ===== READ BY COMPANY =====
    public CompletableFuture<List<WarehouseResponseDTO>> getWarehousesByCompany(Long companyId, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/company/" + companyId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), new TypeReference<List<WarehouseResponseDTO>>() {});
                        } catch (JsonProcessingException e) {
                            throw new CompletionException("Erro ao converter JSON em List<WarehouseResponseDTO>", e);
                        }
                    } else {
                        throw new CompletionException("Erro HTTP ao listar armazéns por empresa: " + response.statusCode(), null);
                    }
                });
    }

    // ===== UPDATE =====
    public CompletableFuture<WarehouseResponseDTO> updateWarehouse(Long id, WarehouseRequestDTO warehouseDTO, String token) {
        try {
            String requestBody = objectMapper.writeValueAsString(warehouseDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), WarehouseResponseDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new CompletionException("Erro ao converter JSON em WarehouseResponseDTO", e);
                            }
                        } else {
                            throw new CompletionException("Erro ao atualizar armazém: " + response.statusCode(), null);
                        }
                    });

        } catch (JsonProcessingException e) {
            throw new CompletionException("Erro ao serializar WarehouseRequestDTO para JSON", e);
        }
    }

    // ===== DELETE =====
    public CompletableFuture<WarehouseResponseDTO> deleteWarehouse(Long id, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), WarehouseResponseDTO.class);
                        } catch (JsonProcessingException e) {
                            throw new CompletionException("Erro ao converter JSON em WarehouseResponseDTO", e);
                        }
                    } else {
                        throw new CompletionException("Erro ao deletar armazém: " + response.statusCode() + " - " + response.body(), null);
                    }
                });
    }
}
