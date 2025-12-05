package org.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.manager.dto.LoginRequestDTO;
import org.manager.dto.LoginResponseDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class UserLoginService {

    private static final String BASE_URL = "http://localhost:8080/auth/login";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public UserLoginService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<LoginResponseDTO> handleLogin(LoginRequestDTO loginRequest) {
        try {
            String requestBody = objectMapper.writeValueAsString(loginRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                LoginResponseDTO resp = objectMapper.readValue(response.body(), LoginResponseDTO.class);
                                return resp;
                            } catch (Exception e) {
                                throw new RuntimeException("Erro ao parsear resposta do login", e);
                            }
                        } else if (response.statusCode() == 403) {
                            throw new RuntimeException("Acesso negado (403): verifique email ou senha");
                        } else {
                            throw new RuntimeException("Falha no login: " + response.statusCode() + " - " + response.body());
                        }
                    });

        } catch (Exception e) {
            CompletableFuture<LoginResponseDTO> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}
