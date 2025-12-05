package org.manager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.manager.dto.TranferRequestDTO;
import org.manager.dto.TransferResponseDTO;
import org.manager.session.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TransferService {
    private  String token ;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private static final String BASE_URL = "http://localhost:8080/api/transfers";

    public TransferService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // ===================== CREATE =====================
    public CompletableFuture<TransferResponseDTO> createAsync(TranferRequestDTO dto) {
        try {
            String jsonBody = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() >= 400) {
                            throw new RuntimeException("Erro ao criar transferência: " + response.body());
                        }
                        try {
                            return objectMapper.readValue(response.body(), TransferResponseDTO.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Erro ao ler resposta JSON", e);
                        }
                    });

        } catch (Exception ex) {
            CompletableFuture<TransferResponseDTO> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
    }

    // ===================== UPDATE =====================
    public CompletableFuture<TransferResponseDTO> updateAsync(Long id, TranferRequestDTO dto) {
        try {
            String jsonBody = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() >= 400) {
                            throw new RuntimeException("Erro ao atualizar transferência: " + response.body());
                        }
                        try {
                            return objectMapper.readValue(response.body(), TransferResponseDTO.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Erro ao ler resposta JSON", e);
                        }
                    });

        } catch (Exception ex) {
            CompletableFuture<TransferResponseDTO> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
    }

    // ===================== DELETE =====================
    public CompletableFuture<Void> deleteAsync(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 400) {
                            throw new RuntimeException("Erro ao deletar transferência: " + response.body());
                        }
                    });

        } catch (Exception ex) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
    }


    // ===================== GET BY ID =====================
    public CompletableFuture<TransferResponseDTO> getByIdAsync(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() >= 400) {
                            throw new RuntimeException("Erro ao buscar transferência: " + response.body());
                        }
                        try {
                            return objectMapper.readValue(response.body(), TransferResponseDTO.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Erro ao processar JSON", e);
                        }
                    });

        } catch (Exception ex) {
            CompletableFuture<TransferResponseDTO> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
    }

    // ===================== GET ALL =====================
    public CompletableFuture<List<TransferResponseDTO>> getAllAsync() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() >= 400) {
                            throw new RuntimeException("Erro ao listar transferências: " + response.body());
                        }
                        try {
                            return objectMapper.readValue(response.body(), new TypeReference<List<TransferResponseDTO>>() {});
                        } catch (Exception e) {
                            throw new RuntimeException("Erro ao processar lista JSON", e);
                        }
                    });

        } catch (Exception ex) {
            CompletableFuture<List<TransferResponseDTO>> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
    }


}
