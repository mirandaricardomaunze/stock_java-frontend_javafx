package org.manager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.manager.dto.TranferRequestDTO;
import org.manager.dto.TransferResponseDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TransferService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private static final String BASE_URL = "http://localhost:8080/api/transfers";

    // 🔹 FORMATO PADRÃO DAS DATAS
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public TransferService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);

        // ✔ Não usar timestamp
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ✔ Ignorar campos desconhecidos
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ===================== CREATE =====================
    public CompletableFuture<TransferResponseDTO> createAsync(
            TranferRequestDTO dto, String token, Long userId) {

        try {
            dto.setUserId(userId);
            String jsonBody = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Authorization", "Bearer " + token)
                    .header("userId", userId.toString())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response ->
                            handleResponse(response, TransferResponseDTO.class, "criar transferência"));

        } catch (Exception ex) {
            CompletableFuture<TransferResponseDTO> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
    }

    // ===================== UPDATE =====================
    public CompletableFuture<TransferResponseDTO> updateAsync(
            Long id, TranferRequestDTO dto, String token, Long userId) {

        try {
            dto.setUserId(userId);
            String jsonBody = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response ->
                            handleResponse(response, TransferResponseDTO.class, "atualizar transferência"));

        } catch (Exception ex) {
            CompletableFuture<TransferResponseDTO> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
    }

    // ===================== DELETE =====================
    public CompletableFuture<Void> deleteAsync(Long id, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 400) {
                            throw new RuntimeException(
                                    "Erro ao deletar transferência: " + response.body());
                        }
                    });

        } catch (Exception ex) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
    }

    // ===================== GET BY ID =====================
    public CompletableFuture<TransferResponseDTO> getByIdAsync(Long id, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response ->
                            handleResponse(response, TransferResponseDTO.class, "buscar transferência"));

        } catch (Exception ex) {
            CompletableFuture<TransferResponseDTO> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
    }

    // ===================== GET ALL =====================
    public CompletableFuture<List<TransferResponseDTO>> getAllAsync(String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response ->
                            handleResponse(response,
                                    new TypeReference<List<TransferResponseDTO>>() {},
                                    "listar transferências"));

        } catch (Exception ex) {
            CompletableFuture<List<TransferResponseDTO>> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
    }

    // ===================== MÉTODOS AUXILIARES =====================
    private <T> T handleResponse(HttpResponse<String> response,
                                 Class<T> clazz, String action) {

        if (response.statusCode() >= 400) {
            throw new RuntimeException(
                    "Erro ao " + action + ": " + response.body());
        }
        try {
            return objectMapper.readValue(response.body(), clazz);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler resposta JSON", e);
        }
    }

    private <T> T handleResponse(HttpResponse<String> response,
                                 TypeReference<T> typeRef, String action) {

        if (response.statusCode() >= 400) {
            throw new RuntimeException(
                    "Erro ao " + action + ": " + response.body());
        }
        try {
            return objectMapper.readValue(response.body(), typeRef);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler resposta JSON", e);
        }
    }
}
