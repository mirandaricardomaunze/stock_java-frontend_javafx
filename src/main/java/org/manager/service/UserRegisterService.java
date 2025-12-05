package org.manager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.manager.dto.UserDTO;
import org.manager.mapper.UserMapper;
import org.manager.util.AlertUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserRegisterService {

    private static final String BASE_URL = "http://localhost:8080/auth";
    private static final String USERS_URL = BASE_URL + "/users";
    private static final String REGISTER_URL = BASE_URL + "/register";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final CompanyService companyService;

    public UserRegisterService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.companyService = new CompanyService();
    }

    // ================= GET ALL USERS =================
    public CompletableFuture<List<UserDTO>> getAllUsers() {
        return getAllUsersFromApi()
                .thenCompose(users -> {
                    List<CompletableFuture<UserDTO>> futures = users.stream()
                            .map(userDto -> companyService.getCompanyById(userDto.getCompanyId())
                                    .thenApply(company -> UserMapper.toDTO(
                                            userDto.toEntity(),
                                            company
                                    )))
                            .toList();

                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> futures.stream()
                                    .map(CompletableFuture::join)
                                    .toList()
                            );
                });
    }

    private CompletableFuture<List<UserDTO>> getAllUsersFromApi() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERS_URL))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> handleResponse(
                        response,
                        new TypeReference<List<UserDTO>>() {},
                        "Erro ao buscar usuários"
                ));
    }

    // ================= CREATE USER =================
    public CompletableFuture<UserDTO> createUser(UserDTO userDTO) {
        try {
            String requestBody = objectMapper.writeValueAsString(userDTO);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(REGISTER_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 201 || response.statusCode() == 200) {
                            return parseJson(response.body(), UserDTO.class, "Erro ao parsear resposta de criação");
                        } else if (response.statusCode() == 400) {
                            AlertUtil.showError("Erro", "Dados inválidos fornecidos.");
                            return null;
                        } else {
                            throw new RuntimeException("Falha ao criar usuário: " + response.statusCode());
                        }
                    });

        } catch (Exception e) {
            CompletableFuture<UserDTO> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Erro ao criar usuário", e));
            return failedFuture;
        }
    }

    // ================= UPDATE USER =================
    public CompletableFuture<UserDTO> updateUser(UserDTO userDTO) {
        try {
            if (userDTO.getId() == null) {
                throw new IllegalArgumentException("ID do usuário não pode ser nulo para atualização.");
            }

            String requestBody = objectMapper.writeValueAsString(userDTO);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(USERS_URL + "/" + userDTO.getId()))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> handleResponse(
                            response,
                            UserDTO.class,
                            "Erro ao atualizar usuário"
                    ));

        } catch (Exception e) {
            CompletableFuture<UserDTO> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Erro ao atualizar usuário", e));
            return failedFuture;
        }
    }

    // ================= DELETE USER =================
    public CompletableFuture<Void> deleteUser(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("ID do usuário não pode ser nulo para exclusão.");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(USERS_URL + "/" + userId))
                    .header("Accept", "application/json")
                    .DELETE()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200 && response.statusCode() != 204) {
                            throw new RuntimeException("Falha ao excluir usuário: " + response.statusCode());
                        }
                    });

        } catch (Exception e) {
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Erro ao excluir usuário", e));
            return failedFuture;
        }
    }

    // ================= HELPERS =================
    private <T> T handleResponse(HttpResponse<String> response, Class<T> clazz, String errorMessage) {
        if (response.statusCode() == 200) {
            return parseJson(response.body(), clazz, errorMessage);
        } else if (response.statusCode() == 404) {
            AlertUtil.showError("Erro", "Recurso não encontrado.");
            return null;
        } else {
            throw new RuntimeException(errorMessage + ": " + response.statusCode());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, TypeReference<T> typeRef, String errorMessage) {
        if (response.statusCode() == 200) {
            return parseJson(response.body(), typeRef, errorMessage);
        } else if (response.statusCode() == 404) {
            AlertUtil.showError("Erro", "Recurso não encontrado.");
            return null;
        } else {
            throw new RuntimeException(errorMessage + ": " + response.statusCode());
        }
    }

    private <T> T parseJson(String json, Class<T> clazz, String errorMessage) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    private <T> T parseJson(String json, TypeReference<T> typeRef, String errorMessage) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException(errorMessage, e);
        }
    }
}
