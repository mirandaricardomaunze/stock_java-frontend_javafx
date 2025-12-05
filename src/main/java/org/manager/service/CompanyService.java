package org.manager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.manager.dto.CompanyDTO;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CompanyService {

    private static final String BASE_URL = "http://localhost:8080/companies";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CompanyService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<List<CompanyDTO>> getAllComapanies() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> {
                    try {
                        return objectMapper.readValue(json, new TypeReference<List<CompanyDTO>>() {});
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao desserializar empresas", e);
                    }
                });
    }
    public  CompletableFuture<List<CompanyDTO>> getAllCompanies(){
        HttpRequest request =HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json->{
                    try {
                       return  objectMapper.readValue(json, new TypeReference<List<CompanyDTO>>() {
                       });
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Não foi possível carregar as empresas "+e.getMessage());
                    }
                });
    }
    public CompletableFuture<CompanyDTO> getCompanyById(Long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> {
                    try {
                        return objectMapper.readValue(json, CompanyDTO.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao desserializar empresa", e);
                    }
                });
    }

    public CompletableFuture<CompanyDTO> createCompany(CompanyDTO companyDTO) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(companyDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(responseJson -> {
                    try {
                        return objectMapper.readValue(responseJson, CompanyDTO.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao criar empresa", e);
                    }
                });
    }

    public CompletableFuture<CompanyDTO> updateCompany(CompanyDTO companyDTO) throws JsonProcessingException {
        if (companyDTO.getId() == null) {
            throw new IllegalArgumentException("ID da empresa não pode ser nulo para atualização.");
        }

        String json = objectMapper.writeValueAsString(companyDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + companyDTO.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(responseJson -> {
                    try {
                        return objectMapper.readValue(responseJson, CompanyDTO.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao atualizar empresa", e);
                    }
                });
    }

    public CompletableFuture<Void> deleteCompany(Long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAccept(response -> {
                    if (response.statusCode() != 204) {
                        throw new RuntimeException("Erro ao excluir empresa. Status: " + response.statusCode());
                    }
                });
    }
}
