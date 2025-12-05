package org.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.manager.dto.MovementRequestDTO;
import org.manager.dto.MovementResponseDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MovementService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final String baseUrl = "http://localhost:8080/api/movements";

    // ================= CREATE =================
    public CompletableFuture<MovementResponseDTO> createAsync(MovementRequestDTO dto, String token) {
        try {
            String json = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        debug("CREATE", request.uri().toString(), response.statusCode(), response.body());

                        if (response.statusCode() != 201) {
                            throw new RuntimeException("Erro ao criar movimento: " + response.body());
                        }

                        try {
                            MovementResponseDTO m = objectMapper.readValue(response.body(), MovementResponseDTO.class);
                            return convertLabelsToPortuguese(m);
                        } catch (Exception e) {
                            throw new RuntimeException("Erro ao converter resposta CREATE: " + e.getMessage());
                        }
                    });

        } catch (Exception e) {
            CompletableFuture<MovementResponseDTO> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    // ================= LIST ALL =================
    public CompletableFuture<List<MovementResponseDTO>> fetchAllAsync(String token) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    debug("LIST", request.uri().toString(), response.statusCode(), response.body());

                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Erro ao buscar movimentos: " + response.statusCode());
                    }

                    try {
                        MovementResponseDTO[] list = objectMapper.readValue(response.body(), MovementResponseDTO[].class);
                        return Arrays.stream(list)
                                .map(this::convertLabelsToPortuguese)
                                .toList();
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao processar lista: " + e.getMessage());
                    }
                });
    }

    // ================= GET BY ID =================
    public CompletableFuture<MovementResponseDTO> fetchByIdAsync(Long id, String token) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + id))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    debug("GET BY ID", request.uri().toString(), response.statusCode(), response.body());

                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Erro ao buscar movimento ID " + id + ": " + response.body());
                    }

                    try {
                        MovementResponseDTO m = objectMapper.readValue(response.body(), MovementResponseDTO.class);
                        return convertLabelsToPortuguese(m);
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao converter GET BY ID: " + e.getMessage());
                    }
                });
    }

    // ================= UPDATE =================
    public CompletableFuture<MovementResponseDTO> updateAsync(Long id, MovementRequestDTO dto, String token) {

        try {
            String json = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/" + id))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        debug("UPDATE", request.uri().toString(), response.statusCode(), response.body());

                        if (response.statusCode() != 200) {
                            throw new RuntimeException("Erro ao atualizar movimento ID " + id + ": " + response.body());
                        }

                        try {
                            MovementResponseDTO m = objectMapper.readValue(response.body(), MovementResponseDTO.class);
                            return convertLabelsToPortuguese(m);
                        } catch (Exception e) {
                            throw new RuntimeException("Erro ao converter UPDATE: " + e.getMessage());
                        }
                    });

        } catch (Exception e) {
            CompletableFuture<MovementResponseDTO> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    // ================= DELETE =================
    public CompletableFuture<Boolean> deleteAsync(Long id, String token) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + id))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    debug("DELETE", request.uri().toString(), response.statusCode(), response.body());
                    return response.statusCode() == 204;
                });
    }

    // ================= FILTER BY DATE =================
    public CompletableFuture<List<MovementResponseDTO>> filterByDateAsync(LocalDateTime start, LocalDateTime end, String token) {

        String url = baseUrl + "/filter?start=" + start + "&end=" + end;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {

                    debug("FILTER BY DATE", request.uri().toString(), response.statusCode(), response.body());

                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Erro ao filtrar movimentos: " + response.body());
                    }

                    try {
                        MovementResponseDTO[] list = objectMapper.readValue(response.body(), MovementResponseDTO[].class);
                        return Arrays.stream(list)
                                .map(this::convertLabelsToPortuguese)
                                .toList();

                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao converter FILTER BY DATE: " + e.getMessage());
                    }
                });
    }

    // ================= DEBUG =================
    private void debug(String action, String url, int status, String body) {
        System.out.println("\n========== DEBUG MOVEMENT API (" + action + ") ==========");
        System.out.println("URL: " + url);
        System.out.println("Status: " + status);
        System.out.println("Body: " + body);
        System.out.println("========================================================\n");
    }

    // ================= LABEL CONVERTER =================
    private MovementResponseDTO convertLabelsToPortuguese(MovementResponseDTO m) {
        if (m == null) return null;

        m.setType(mapType(m.getType()));
        m.setOrigin(mapOrigin(m.getOrigin()));
        m.setStatus(mapStatus(m.getStatus()));

        return m;
    }

    private String mapType(String t) {
        return switch (t) {
            case "IN" -> "Entrada";
            case "OUT" -> "Saída";
            case "TRANSFER" -> "Transferência";
            case "RETURN" -> "Devolução";
            case "STOCK_ADJUST" -> "Ajuste de Estoque";
            default -> t;
        };
    }

    private String mapOrigin(String o) {
        return switch (o) {
            case "ORDER" -> "Pedido";
            case "INVOICE" -> "Fatura";
            case "POS" -> "PDV";
            case "SYSTEM" -> "Sistema";
            default -> o;
        };
    }

    private String mapStatus(String s) {
        return switch (s) {
            case "PENDING" -> "Pendente";
            case "COMPLETED" -> "Concluído";
            case "CANCELLED" -> "Cancelado";
            default -> s;
        };
    }
}
