package org.manager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.manager.dto.MonthlyMovementDTO;
import org.manager.dto.SaleDTO;
import org.manager.dto.SaleRequestDTO;
import org.manager.model.PageResponse;
import org.manager.util.AlertUtil;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class SaleService {

    private final String BASE_URL = "http://localhost:8080/api/sales";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SaleService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // ----------------- CRIAR VENDA -----------------
    public CompletableFuture<SaleDTO> createSale(SaleRequestDTO saleRequest, String token) {
        try {

            String requestBody = objectMapper.writeValueAsString(saleRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {

                        System.out.println("🔹 [createSale] Status: " + response.statusCode());
                        System.out.println("📦 [createSale] Body: " + response.body());

                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            try {
                                return objectMapper.readValue(response.body(), SaleDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new CompletionException(" Erro ao processar JSON da venda", e);
                            }
                        } else {
                            throw new CompletionException(
                                    new RuntimeException(" Falha ao criar venda. Código: "
                                            + response.statusCode() + " - Body: " + response.body())
                            );
                        }
                    })
                    .exceptionally(ex -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        AlertUtil.showError("Erro ao criar venda", cause.getMessage());
                        cause.printStackTrace();
                        return null;
                    });

        } catch (JsonProcessingException e) {
            throw new CompletionException(" Erro ao serializar venda para JSON", e);
        }
    }

    public CompletableFuture<List<SaleDTO>> listSales(String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        System.out.println("🔹 [listSales] Status: " + response.statusCode());
                        System.out.println("📦 [listSales] Body: " + response.body());

                        if (response.statusCode() == 200) {
                            try {
                                JavaType type = objectMapper.getTypeFactory()
                                        .constructParametricType(PageResponse.class, SaleDTO.class);

                                PageResponse<SaleDTO> page = objectMapper.readValue(response.body(), type);

                                return page.getContent(); // ✅ agora funciona
                            } catch (Exception e) {
                                throw new CompletionException("Erro ao desserializar JSON de vendas", e);
                            }
                        } else {
                            throw new CompletionException(
                                    new RuntimeException("Falha ao buscar vendas. Código: " + response.statusCode())
                            );
                        }
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            System.out.println("❌ Erro ao carregar vendas: " + ex.getMessage());
                            AlertUtil.showError("Erro", "Falha ao carregar vendas: " + ex.getMessage());
                            ex.printStackTrace();
                        });
                        return null;
                    });

        } catch (Exception e) {
            throw new CompletionException("Erro ao listar vendas", e);
        }
    }




    // ----------------- BUSCAR VENDA POR ID -----------------
    public CompletableFuture<SaleDTO> getSaleById(Long id, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {

                        System.out.println("🔹 [getSaleById] Status: " + response.statusCode());
                        System.out.println("📦 [getSaleById] Body: " + response.body());

                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), SaleDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new CompletionException("Erro ao processar JSON da venda", e);
                            }
                        } else {
                            throw new CompletionException(
                                    new RuntimeException(" Falha ao buscar venda. Código: " + response.statusCode())
                            );
                        }
                    })
                    .exceptionally(ex -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        AlertUtil.showError("Erro ao buscar venda", cause.getMessage());
                        cause.printStackTrace();
                        return null;
                    });

        } catch (Exception e) {
            throw new CompletionException("❌ Erro ao buscar venda por ID", e);
        }
    }

    // ----------------- DELETAR VENDA -----------------
    public CompletableFuture<Void> deleteSale(Long id, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {

                        System.out.println("🔹 [deleteSale] Status: " + response.statusCode());
                        System.out.println("📦 [deleteSale] Body: " + response.body());

                        if (response.statusCode() != 200 && response.statusCode() != 204) {
                            throw new CompletionException(new RuntimeException(
                                    "❌ Falha ao deletar venda. Código: " + response.statusCode()
                            ));
                        }
                    })
                    .exceptionally(ex -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        AlertUtil.showError("Erro ao deletar venda", cause.getMessage());
                        cause.printStackTrace();
                        return null;
                    });

        } catch (Exception e) {
            throw new CompletionException("❌ Erro ao deletar venda", e);
        }
    }

    public CompletableFuture<BigDecimal> getTotalSales(String period, String token) {
        String url = BASE_URL + "/total?period=" + period;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {

                    if (response.statusCode() != 200) {
                        throw new CompletionException(new RuntimeException(
                                "Erro ao buscar total de vendas. Código: " + response.statusCode()));
                    }

                    String body = response.body().trim();
                    try {
                        return new BigDecimal(body); // 👈 O CERTO
                    } catch (Exception e) {
                        throw new CompletionException(
                                new RuntimeException("Valor inválido retornado pelo servidor: " + body));
                    }
                })
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

                    Platform.runLater(() -> {
                        AlertUtil.showError("Erro ao buscar total de vendas", cause.getMessage());
                    });

                    return BigDecimal.ZERO;
                });
    }

    // ----------------- BUSCAR LUCRO -----------------
    public CompletableFuture<BigDecimal> getProfit(String period, String token) {
        String url = BASE_URL + "/profit?period=" + period;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {

                    if (response.statusCode() != 200) {
                        throw new CompletionException(new RuntimeException(
                                "Erro ao buscar lucro. Código: " + response.statusCode()));
                    }

                    String body = response.body().trim();
                    try {
                        return new BigDecimal(body); // converte String para BigDecimal
                    } catch (Exception e) {
                        throw new CompletionException(
                                new RuntimeException("Valor inválido retornado pelo servidor: " + body));
                    }
                })
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

                    Platform.runLater(() -> {
                        AlertUtil.showError("Erro ao buscar lucro", cause.getMessage());
                    });

                    return BigDecimal.ZERO; // retorna 0 em caso de erro
                });
    }
    // ================== MOVIMENTAÇÃO MENSAL ==================
    public CompletableFuture<List<MonthlyMovementDTO>> getMonthlyMovement(Long companyId, String period, String token) {
        String url = BASE_URL + "/movement?companyId=" + companyId + "&period=" + period;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new CompletionException(new RuntimeException(
                                "Erro ao buscar movimentação. Código: " + response.statusCode()
                        ));
                    }

                    try {
                        return objectMapper.readValue(response.body(), new TypeReference<List<MonthlyMovementDTO>>() {});
                    } catch (Exception e) {
                        throw new CompletionException(new RuntimeException(
                                "Erro ao desserializar movimentação: " + response.body(), e
                        ));
                    }
                })
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    System.out.println("Erro ao buscar movimentação"+ cause.getMessage());
                    cause.printStackTrace();
                    return Collections.emptyList(); // retorna lista vazia em vez de null
                });
    }


}


