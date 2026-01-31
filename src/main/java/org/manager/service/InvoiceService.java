package org.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.manager.dto.InvoiceDTO;
import org.manager.dto.OrderDTO;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InvoiceService {

    private static final String BASE_URL = "http://localhost:8080/api/invoices";
    private static final String ORDER_BASE_URL = "http://localhost:8080/api/orders";

    private final HttpClient client;
    private final ObjectMapper mapper;

    public InvoiceService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /** 🔹 Criar fatura por número do pedido */
    public CompletableFuture<InvoiceDTO> createInvoiceFromOrderNumber(String orderNumber, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?orderNumber=" + URLEncoder.encode(orderNumber, StandardCharsets.UTF_8)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        int status = response.statusCode();
                        if (status == 200 || status == 201) {
                            return mapper.readValue(response.body(), InvoiceDTO.class);
                        } else {
                            throw new RuntimeException("Erro ao criar fatura. Status: " + status + " → " + response.body());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao processar resposta da fatura", e);
                    }
                });
    }

    /** 🔹 Buscar todas as faturas */
    public CompletableFuture<List<InvoiceDTO>> fetchInvoicesAsync(String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        String body = response.body();
                        if (body == null || body.isBlank() || body.equals("[]")) {
                            return List.of();
                        }
                        InvoiceDTO[] invoices = mapper.readValue(body, InvoiceDTO[].class);
                        return Arrays.asList(invoices);
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao converter lista de faturas", e);
                    }
                });
    }

    /** 🔹 Buscar fatura por ID */
    public CompletableFuture<InvoiceDTO> fetchInvoiceById(Long id, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        if (response.statusCode() == 200) {
                            return mapper.readValue(response.body(), InvoiceDTO.class);
                        } else if (response.statusCode() == 404) {
                            return null;
                        } else {
                            throw new RuntimeException("Erro ao buscar fatura. Status: " + response.statusCode());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao processar fatura por ID", e);
                    }
                });
    }

    /** 🔹 Cancelar fatura com devolução de estoque */
    public CompletableFuture<InvoiceDTO> cancelInvoice(Long id, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id + "/cancel"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        if (response.statusCode() == 200) {
                            return mapper.readValue(response.body(), InvoiceDTO.class);
                        } else {
                            throw new RuntimeException("Erro ao cancelar fatura. Status: " + response.statusCode() + " → " + response.body());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao processar resposta do cancelamento", e);
                    }
                });
    }

    /** 🔹 Excluir fatura */
    public CompletableFuture<Void> deleteInvoice(Long id, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .header("Authorization", "Bearer " + token)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 204 && response.statusCode() != 200) {
                        throw new RuntimeException("Erro ao excluir fatura: " + response.statusCode());
                    }
                    return null;
                });
    }

    /** 🔹 Atualizar status da fatura */
    public CompletableFuture<InvoiceDTO> updateInvoiceStatus(Long id, String status, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id + "/status/" + URLEncoder.encode(status, StandardCharsets.UTF_8)))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        if (response.statusCode() == 200) {
                            return mapper.readValue(response.body(), InvoiceDTO.class);
                        } else {
                            throw new RuntimeException("Erro ao atualizar status da fatura. Status: " + response.statusCode() + " → " + response.body());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao processar resposta da atualização de status", e);
                    }
                });
    }

    /** 🔹 Exportar PDF */
    public CompletableFuture<byte[]> exportPdfAsync(Long id, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id + "/export/pdf"))
                .GET()
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/pdf")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() != 200)
                        throw new RuntimeException("Erro ao exportar PDF. Status: " + response.statusCode());
                    return response.body();
                });
    }

    /** 🔹 Exportar Excel */
    public CompletableFuture<byte[]> exportExcelAsync(Long id, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id + "/export/excel"))
                .GET()
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() != 200)
                        throw new RuntimeException("Erro ao exportar Excel. Status: " + response.statusCode());
                    return response.body();
                });
    }

    /** 🔹 Exportar HTML (simplesmente PDF convertido para string) */
    public CompletableFuture<String> exportHtmlAsync(Long id, String token) {
        return exportPdfAsync(id, token).thenApply(bytes -> new String(bytes));
    }

    /** 🔹 Buscar todos os pedidos */
    public CompletableFuture<List<OrderDTO>> fetchOrdersAsync(String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ORDER_BASE_URL))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        if (response.statusCode() != 200)
                            throw new RuntimeException("Erro ao buscar pedidos. Status: " + response.statusCode());
                        OrderDTO[] orders = mapper.readValue(response.body(), OrderDTO[].class);
                        return Arrays.asList(orders);
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao converter lista de pedidos", e);
                    }
                });
    }

    /** 🔹 Pesquisar pedidos (por nome ou número) */
    public CompletableFuture<List<OrderDTO>> searchOrdersAsync(String query, String token) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ORDER_BASE_URL + "/search?q=" + encoded))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        try {
                            if (response.statusCode() != 200)
                                throw new RuntimeException("Erro ao pesquisar pedidos. Status: " + response.statusCode());
                            OrderDTO[] orders = mapper.readValue(response.body(), OrderDTO[].class);
                            return Arrays.asList(orders);
                        } catch (Exception e) {
                            throw new RuntimeException("Erro ao converter pesquisa de pedidos", e);
                        }
                    });
        } catch (Exception e) {
            CompletableFuture<List<OrderDTO>> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}
