package org.manager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.manager.dto.SupplierDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class SupplierService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String BASE_URL = "http://localhost:8080/suppliers";

    public SupplierService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // Criar fornecedor
    public CompletableFuture<SupplierDTO> createSupplier(SupplierDTO supplierDTO, String token) {
        try {
            String requestBody = objectMapper.writeValueAsString(supplierDTO);
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
                                return objectMapper.readValue(response.body(), SupplierDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new CompletionException("Erro ao processar JSON da resposta", e);
                            }
                        } else {
                            throw new CompletionException(
                                    new RuntimeException("Falha ao criar fornecedor. Código: " + response.statusCode() + " - " + response.body())
                            );
                        }
                    });

        } catch (JsonProcessingException e) {
            throw new CompletionException("Erro ao serializar fornecedor para JSON", e);
        }
    }

    // Listar todos fornecedores
    public CompletableFuture<List<SupplierDTO>> getAllSuppliers(String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), new TypeReference<List<SupplierDTO>>() {});
                        } catch (JsonProcessingException e) {
                            throw new CompletionException("Erro ao converter JSON da resposta (lista)", e);
                        }
                    } else {
                        throw new CompletionException(
                                new RuntimeException("Falha ao buscar fornecedores. Código: " + response.statusCode() + " - " + response.body())
                        );
                    }
                });
    }

    // Buscar fornecedor por ID
    public CompletableFuture<SupplierDTO> getSupplierById(Long id, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), SupplierDTO.class);
                        } catch (JsonProcessingException e) {
                            throw new CompletionException("Erro ao processar JSON da resposta", e);
                        }
                    } else {
                        throw new CompletionException(
                                new RuntimeException("Fornecedor não encontrado. Código: " + response.statusCode() + " - " + response.body())
                        );
                    }
                });
    }

    // Atualizar fornecedor
    public CompletableFuture<SupplierDTO> updateSupplier(SupplierDTO supplierDTO, Long id, String token) {
        try {
            String requestBody = objectMapper.writeValueAsString(supplierDTO);
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
                                return objectMapper.readValue(response.body(), SupplierDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new CompletionException("Erro ao processar JSON da resposta", e);
                            }
                        } else {
                            throw new CompletionException(
                                    new RuntimeException("Falha ao atualizar fornecedor. Código: " + response.statusCode() + " - " + response.body())
                            );
                        }
                    });
        } catch (JsonProcessingException e) {
            throw new CompletionException("Erro ao serializar fornecedor para JSON", e);
        }
    }

    // Deletar fornecedor
    public CompletableFuture<Void> deleteSupplier(Long id, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        return null;
                    } else {
                        throw new CompletionException(
                                new RuntimeException("Falha ao deletar fornecedor. Código: " + response.statusCode() + " - " + response.body())
                        );
                    }
                });
    }
}
