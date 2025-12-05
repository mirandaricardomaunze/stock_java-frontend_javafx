package org.manager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.manager.dto.CategoryDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class CategoryService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String BASE_URL = "http://localhost:8080/categories";

    public CategoryService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // Criar categoria
    public CompletableFuture<CategoryDTO> createCategory(CategoryDTO categoryDTO, String token) {
        try {
            String requestBody = objectMapper.writeValueAsString(categoryDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        // Aceitar 200 ou 201 como sucesso
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            try {
                                return objectMapper.readValue(response.body(), CategoryDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new CompletionException("Falha ao processar JSON da resposta", e);
                            }
                        } else {
                            throw new CompletionException(
                                    new RuntimeException("Erro ao criar categoria: " + response.statusCode() + " - " + response.body())
                            );
                        }
                    });

        } catch (JsonProcessingException e) {
            throw new CompletionException("Falha ao serializar categoria", e);
        }
    }

    // Listar todas as categorias
    public CompletableFuture<List<CategoryDTO>> getAllCategories(String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), new TypeReference<List<CategoryDTO>>() {});
                        } catch (Exception e) {
                            throw new RuntimeException("Falha ao converter JSON para List<CategoryDTO>", e);
                        }
                    } else {
                        throw new RuntimeException("Erro HTTP: " + response.statusCode());
                    }
                });
    }

    // Atualizar categoria
    public CompletableFuture<CategoryDTO> updateCategory(Long id, CategoryDTO categoryDTO, String token) {
        try {
            String requestBody = objectMapper.writeValueAsString(categoryDTO);
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
                                return objectMapper.readValue(response.body(), CategoryDTO.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Falha ao converter JSON da resposta", e);
                            }
                        } else {
                            throw new RuntimeException("Erro HTTP: " + response.statusCode());
                        }
                    });

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Deletar categoria
    public CompletableFuture<CategoryDTO> deleteCategory(Long id, String token) {
        try {
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
                                return objectMapper.readValue(response.body(), CategoryDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException("Falha ao processar JSON do DELETE", e);
                            }
                        } else {
                            throw new RuntimeException("Erro HTTP ao deletar categoria: " + response.statusCode());
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long getCategoryId(String categoryName, String token) {
        return getAllCategories(token)
                .thenApply(categories -> categories.stream()
                        .filter(c -> c.getName().equals(categoryName))
                        .map(CategoryDTO::getId)
                        .findFirst()
                        .orElse(null)
                )
                .join();
    }

    public String getCategoryName(Long categoryId, String token) {
        return getAllCategories(token)
                .thenApply(categories -> categories.stream()
                        .filter(c -> c.getId().equals(categoryId))
                        .map(CategoryDTO::getName)
                        .findFirst()
                        .orElse(null)
                )
                .join();
    }

    public CompletableFuture<Long> getTotalOfCategoriesByCompanyId(Long companyId, String token) {
        String url = BASE_URL + "/total/" + companyId + "/total-categories";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return Long.parseLong(response.body());
                        } catch (Exception e) {
                            throw new RuntimeException("Erro ao buscar total de categorias: " + e.getMessage());
                        }
                    } else {
                        throw new RuntimeException("Erro HTTP: " + response.statusCode());
                    }
                });
    }

}
