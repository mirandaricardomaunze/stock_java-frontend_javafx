package org.manager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.manager.dto.MonthlyMovementDTO;
import org.manager.dto.SaleResponseDTO;
import org.manager.dto.SaleRequestDTO;
import org.manager.model.PageResponse;
import org.manager.util.AlertUtil;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    // ================= CREATE SALE =================
    public CompletableFuture<SaleResponseDTO> createSale(SaleRequestDTO saleRequest, String token,Long userId) {
        try {
            String requestBody = objectMapper.writeValueAsString(saleRequest);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .header("userId", String.valueOf(userId))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            try {
                                return objectMapper.readValue(response.body(), SaleResponseDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new CompletionException("Erro ao processar JSON da venda", e);
                            }
                        } else {
                            throw new CompletionException(new RuntimeException(
                                    "Falha ao criar venda. Código: " + response.statusCode() + " - " + response.body()
                            ));
                        }
                    })
                    .exceptionally(ex -> handleException("Erro ao criar venda", ex));
        } catch (JsonProcessingException e) {
            throw new CompletionException("Erro ao serializar venda para JSON", e);
        }
    }

    // ================= LIST SALES =================
    public CompletableFuture<List<SaleResponseDTO>> listSales(String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                JavaType type = objectMapper.getTypeFactory()
                                        .constructParametricType(PageResponse.class, SaleResponseDTO.class);
                                PageResponse<SaleResponseDTO> page = objectMapper.readValue(response.body(), type);
                                return page.getContent();
                            } catch (Exception e) {
                                throw new CompletionException("Erro ao desserializar JSON de vendas", e);
                            }
                        } else {
                            throw new CompletionException(new RuntimeException(
                                    "Falha ao buscar vendas. Código: " + response.statusCode()
                            ));
                        }
                    })
                    .exceptionally(ex -> handleExceptionList("Erro ao listar vendas", ex));
        } catch (Exception e) {
            throw new CompletionException("Erro ao listar vendas", e);
        }
    }

    // ================= GET SALE BY ID =================
    public CompletableFuture<SaleResponseDTO> getSaleById(Long id, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), SaleResponseDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new CompletionException("Erro ao processar JSON da venda", e);
                            }
                        } else {
                            throw new CompletionException(new RuntimeException(
                                    "Falha ao buscar venda. Código: " + response.statusCode()
                            ));
                        }
                    })
                    .exceptionally(ex -> handleException("Erro ao buscar venda", ex));
        } catch (Exception e) {
            throw new CompletionException("Erro ao buscar venda por ID", e);
        }
    }

    // ================= CANCEL SALE =================
    public CompletableFuture<SaleResponseDTO> cancelSale(Long id, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id + "/cancel"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .method("PATCH", HttpRequest.BodyPublishers.noBody())
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), SaleResponseDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new CompletionException("Erro ao processar JSON da venda cancelada", e);
                            }
                        } else {
                            throw new CompletionException(new RuntimeException(
                                    "Falha ao cancelar venda. Código: " + response.statusCode() + " - " + response.body()
                            ));
                        }
                    })
                    .exceptionally(ex -> handleException("Erro ao cancelar venda", ex));
        } catch (Exception e) {
            throw new CompletionException("Erro ao cancelar venda", e);
        }
    }

    // ================= DELETE SALE =================
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
                        if (response.statusCode() != 200 && response.statusCode() != 204) {
                            throw new CompletionException(new RuntimeException(
                                    "Falha ao deletar venda. Código: " + response.statusCode()
                            ));
                        }
                    })
                    .exceptionally(ex -> {
                        handleException("Erro ao deletar venda", ex);
                        return null;
                    });
        } catch (Exception e) {
            throw new CompletionException("Erro ao deletar venda", e);
        }
    }

    // ================= TOTAL SALES =================
    public CompletableFuture<BigDecimal> getTotalSales(String period, String token) {
        String url = BASE_URL + "/total?period=" + period;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> parseBigDecimalResponse(response, "total de vendas"));
    }

    // ================= PROFIT =================
    public CompletableFuture<BigDecimal> getProfit(String period, String token) {
        String url = BASE_URL + "/profit?period=" + period;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> parseBigDecimalResponse(response, "lucro"));
    }

    // ================= MONTHLY MOVEMENT =================
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
                    Platform.runLater(() -> AlertUtil.showError("Erro ao buscar movimentação", cause.getMessage()));
                    cause.printStackTrace();
                    return Collections.emptyList();
                });
    }

    // ================= EXPORT METHODS =================
    public CompletableFuture<byte[]> exportSaleToPdf(Long saleId, String token) {
        return exportSaleReport(saleId, "pdf", token);
    }

    public CompletableFuture<byte[]> exportSaleToHtml(Long saleId, String token) {
        return exportSaleReport(saleId, "html", token);
    }

    public CompletableFuture<byte[]> exportSaleToExcel(Long saleId, String token) {
        return exportSaleReport(saleId, "xlsx", token);
    }

    private CompletableFuture<byte[]> exportSaleReport(Long saleId, String format, String token) {
        if (saleId == null || saleId <= 0) {
            Platform.runLater(() -> AlertUtil.showError("Erro ao exportar venda", "ID da venda inválido"));
            return CompletableFuture.failedFuture(new IllegalArgumentException("ID da venda inválido"));
        }

        try {
            String url;
            switch (format.toLowerCase()) {
                case "pdf": url = BASE_URL + "/" + saleId + "/report/pdf"; break;
                case "html": url = BASE_URL + "/" + saleId + "/report/html"; break;
                case "xlsx":
                case "excel": url = BASE_URL + "/" + saleId + "/report/excel"; break;
                default: throw new IllegalArgumentException("Formato de exportação inválido: " + format);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) return response.body();
                        else {
                            String message = new String(response.body());
                            throw new CompletionException(new RuntimeException(
                                    "Falha ao exportar venda para " + format +
                                            ". Código: " + response.statusCode() +
                                            ". Mensagem: " + message
                            ));
                        }
                    })
                    .exceptionally(ex -> handleExportException(ex));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private byte[] handleExportException(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        Platform.runLater(() -> AlertUtil.showError("Erro ao exportar venda", cause.getMessage()));
        cause.printStackTrace();
        throw new CompletionException(cause);
    }

    // ================= HELPER METHODS =================
    private SaleResponseDTO handleException(String title, Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        Platform.runLater(() -> AlertUtil.showError(title, cause.getMessage()));
        cause.printStackTrace();
        return null;
    }

    private List<SaleResponseDTO> handleExceptionList(String title, Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        Platform.runLater(() -> AlertUtil.showError(title, cause.getMessage()));
        cause.printStackTrace();
        return Collections.emptyList();
    }

    private BigDecimal parseBigDecimalResponse(HttpResponse<String> response, String type) {
        if (response.statusCode() != 200) {
            throw new CompletionException(new RuntimeException("Erro ao buscar " + type + ". Código: " + response.statusCode()));
        }
        try {
            return new BigDecimal(response.body().trim());
        } catch (Exception e) {
            throw new CompletionException(new RuntimeException("Valor inválido retornado pelo servidor: " + response.body()));
        }
    }
}
