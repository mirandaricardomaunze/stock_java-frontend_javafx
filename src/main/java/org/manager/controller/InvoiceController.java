package org.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import org.manager.dto.InvoiceDTO;
import org.manager.dto.OrderDTO;
import org.manager.service.InvoiceService;
import org.manager.service.OrderService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceController {

    // 🔍 Campos de pesquisa
    @FXML private TextField searchOrderField;
    @FXML private TextField searchInvoiceField;

    //Tabelas de Pedidos e Faturas
    @FXML private TableView<OrderDTO> orderTable;
    @FXML private TableColumn<OrderDTO, String> colOrderNumber;
    @FXML private TableColumn<OrderDTO, String> colCustomer;
    @FXML private TableColumn<OrderDTO, String> colDate;
    @FXML private TableColumn<OrderDTO, Double> colTotal;

    //Invoices table
    @FXML private TableView<InvoiceDTO> invoiceTable;
    @FXML private TableColumn<InvoiceDTO, String> colInvOrderNumber;
    @FXML private TableColumn<InvoiceDTO, String> colInvCustomer;
    @FXML private TableColumn<InvoiceDTO, String> colInvDate;
    @FXML private TableColumn<InvoiceDTO, Double> colInvTotal;

    // ⚙️ Botões
    @FXML private Button btnCreateInvoice;
    @FXML private Button btnExportPdf;
    @FXML private Button btnExportExcel;
    @FXML private Button btnPrintInvoice;

    // 🧠 Dados
    private final ObservableList<OrderDTO> orders = FXCollections.observableArrayList();
    private final ObservableList<InvoiceDTO> invoices = FXCollections.observableArrayList();

    private final String token = SessionManager.getToken();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OrderService orderService;
    private final InvoiceService invoiceService;
    private final ObjectMapper objectMapper;

    public InvoiceController() {
        this.orderService = new OrderService();
        this.invoiceService = new InvoiceService();
        this.objectMapper = new ObjectMapper();
    }

    // --- INICIALIZAÇÃO ---
    @FXML
    public void initialize() {
        setupOrderTable();
        setupInvoiceTable();
        loadOrders();
        loadInvoices();
        // 🔍 Pesquisa em tempo real
        searchOrderField.textProperty().addListener((obs, old, newValue) -> filterOrders(newValue));
        searchInvoiceField.textProperty().addListener((obs, old, newValue) -> filterInvoices(newValue));
        startOrderAutoRefresh();
    }
  //tables settings
    private void setupOrderTable() {
        colOrderNumber.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getOrderNumber()));
        colCustomer.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCustomerName()));
        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getOrderDate() != null
                                ? DATE_FORMATTER.format(data.getValue().getOrderDate())
                                : ""));
        colTotal.setCellValueFactory(data ->
                new SimpleDoubleProperty(
                        data.getValue().getTotalAmount() != null
                                ? data.getValue().getTotalAmount().doubleValue()
                                : 0.0).asObject());
        orderTable.setItems(orders);
    }

    private void setupInvoiceTable() {
        colInvOrderNumber.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getInvoiceNumber()));
        colInvCustomer.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCustomerName()));
        colInvDate.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getInvoiceDate() != null
                                ? DATE_FORMATTER.format(data.getValue().getInvoiceDate())
                                : ""));
        colInvTotal.setCellValueFactory(data ->
                new SimpleDoubleProperty(
                        data.getValue().getTotalAmount() != null
                                ? data.getValue().getTotalAmount().doubleValue()
                                : 0.0).asObject());
        invoiceTable.setItems(invoices);
    }

    // --- loaging data---
    private void loadOrders() {
        orderService.fetchOrdersAsync(token)
                .thenAccept(list -> Platform.runLater(() -> {
                    orders.setAll(list);
                    filterOrders(searchOrderField.getText());
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Falha ao carregar pedidos: " + ex.getMessage()));
                    return null;
                });
    }

    private void loadInvoices() {
        invoiceService.fetchInvoicesAsync(token)
                .thenAccept(list -> Platform.runLater(() -> {
                    invoices.setAll(list);
                    filterInvoices(searchInvoiceField.getText());
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Falha ao carregar faturas: " + ex.getMessage()));
                    return null;
                });
    }
     @FXML
     public void filterOrders(){
        filterOrders(searchOrderField.getText());
     }
    @FXML
    public void filterInvoices(){
        filterOrders(searchInvoiceField.getText());
    }

    // --- FILTROS ---
    private void filterOrders(String query) {
        if (query == null || query.isBlank()) {
            orderTable.setItems(orders);
            return;
        }
        String lower = query.toLowerCase();
        List<OrderDTO> filtered = orders.stream()
                .filter(o -> (o.getOrderNumber() != null && o.getOrderNumber().toLowerCase().contains(lower))
                        || (o.getCustomerName() != null && o.getCustomerName().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
        orderTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void filterInvoices(String query) {
        if (query == null || query.isBlank()) {
            invoiceTable.setItems(invoices);
            return;
        }
        String lower = query.toLowerCase();
        List<InvoiceDTO> filtered = invoices.stream()
                .filter(i -> (i.getInvoiceNumber() != null && i.getInvoiceNumber().toLowerCase().contains(lower))
                        || (i.getCustomerName() != null && i.getCustomerName().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
        invoiceTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void createInvoiceFromSelectedOrder() {
        OrderDTO selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("Atenção", "Selecione um pedido para faturar.");
            return;
        }

        invoiceService.createInvoiceFromOrderNumber(selected.getOrderNumber(), token)
                .thenAccept(invoice -> Platform.runLater(() -> {
                    invoices.add(invoice);
                    AlertUtil.showInfo("Sucesso", "Fatura criada com sucesso!");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Falha ao criar fatura: " + ex.getMessage()));
                    return null;
                });
    }

    //Method to export files of orders
    @FXML private void exportOrderFilePdf(){exportOrderPdfAndExcel("pdf");};
    @FXML private void exportOrderFileExcel(){exportOrderPdfAndExcel("excel");};
    private void exportOrderPdfAndExcel(String type){
        OrderDTO selected=orderTable.getSelectionModel().getSelectedItem();
        if (selected==null){
            AlertUtil.showInfo("Atenção", "Selecione uma fatura para exportar.");
            return;
        }
        var typeFile=type.equalsIgnoreCase("pdf")?
                orderService.exportOrderPdf(selected.getId(),token):
                orderService.exportOrderExcel(selected.getId(),token);
        typeFile.thenAccept(bytes -> saveFile(bytes,"order_"+selected.getOrderNumber()+"."+type))
                .exceptionally(ex->{Platform.runLater(()->{
                    System.out.println( "Falha ao exportar " + type.toUpperCase() + ": " + ex.getMessage());
                    AlertUtil.showError("Erro", "Falha ao exportar " + type.toUpperCase() + ": " + ex.getMessage());
                });
                    return  null;
                });
    };


    @FXML private void exportInvoiceFilePdf() { exportInvoiceFile("pdf"); }
    @FXML private void exportInvoiceFileExcel() { exportInvoiceFile("excel"); }

    private void exportInvoiceFile(String type) {
        InvoiceDTO selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("Atenção", "Selecione uma fatura para exportar.");
            return;
        }

        var future = type.equalsIgnoreCase("pdf")
                ? invoiceService.exportPdfAsync(selected.getId(), token)
                : invoiceService.exportExcelAsync(selected.getId(), token);

        future.thenAccept(bytes -> saveFile(bytes, "invoice_" + selected.getInvoiceNumber() + "." + type))
                .exceptionally(ex -> {
                    Platform.runLater(() ->{
                            System.out.println( "Falha ao exportar " + type.toUpperCase() + ": " + ex.getMessage());
                            AlertUtil.showError("Erro", "Falha ao exportar " + type.toUpperCase() + ": " + ex.getMessage());
                    });
                    return null;

                });
    }

    @FXML
    private void cancelSelectedInvoice() {
        InvoiceDTO selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("Atenção", "Selecione uma fatura para cancelar.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText("Cancelar fatura");
        alert.setContentText("Deseja realmente cancelar a fatura " + selected.getInvoiceNumber() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                invoiceService.cancelInvoice(selected.getId(), token)
                        .thenAccept(invoice -> Platform.runLater(() -> {
                            // Atualiza a lista de faturas
                            int index = invoices.indexOf(selected);
                            if (index >= 0) {
                                invoices.set(index, invoice);
                            }
                            AlertUtil.showInfo("Sucesso", "Fatura cancelada com sucesso!");
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() ->
                                    AlertUtil.showError("Erro", "Falha ao cancelar fatura: " + ex.getMessage()));
                            return null;
                        });
            }
        });
    }


    @FXML
    private void printInvoice() {
        InvoiceDTO selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("Atenção", "Selecione uma fatura para imprimir.");
            return;
        }

        invoiceService.exportPdfAsync(selected.getId(), token)
                .thenAccept(bytes -> Platform.runLater(() -> {
                    try {
                        File tempFile = File.createTempFile("fatura_" + selected.getInvoiceNumber(), ".pdf");
                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                            fos.write(bytes);
                        }

                        var desktop = java.awt.Desktop.getDesktop();
                        if (desktop.isSupported(java.awt.Desktop.Action.PRINT)) {
                            desktop.print(tempFile);
                            AlertUtil.showInfo("Impressão", "Fatura enviada para a impressora.");
                        } else {
                            AlertUtil.showError("Erro", "Impressão direta não suportada neste sistema.");
                        }
                    } catch (Exception e) {
                        AlertUtil.showError("Erro", "Falha ao imprimir: " + e.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Falha ao gerar PDF: " + ex.getMessage()));
                    return null;
                });
    }

    // --- REFRESH AUTOMÁTICO ---
    private void startOrderAutoRefresh() {
        Thread refreshThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(15000); // Atualiza a cada 15 segundos
                    orderService.fetchOrdersAsync(token)
                            .thenAccept(list -> Platform.runLater(() -> {
                                orders.setAll(list);
                                filterOrders(searchOrderField.getText());
                            }));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception ignored) {}
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    // --- SALVAR ARQUIVO ---
    private void saveFile(byte[] bytes, String fileName) {
        new Thread(() -> {
            try {
                File file = new File(System.getProperty("user.home") + "/Downloads/" + fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(bytes);
                }
                Platform.runLater(() ->
                        AlertUtil.showInfo("Sucesso", "Arquivo salvo em: " + file.getAbsolutePath()));
            } catch (Exception e) {
                Platform.runLater(() ->
                        AlertUtil.showError("Erro", "Falha ao salvar arquivo: " + e.getMessage()));
            }
        }).start();
    }
}
