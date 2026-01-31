package org.manager.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.manager.dto.SaleResponseDTO;
import org.manager.service.SaleService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class SaleController {

    @FXML private TableView<SaleResponseDTO> salesTable;
    @FXML private TableColumn<SaleResponseDTO, String> clientColumn;
    @FXML private TableColumn<SaleResponseDTO, String> codeColumn;
    @FXML private TableColumn<SaleResponseDTO, String> productColumn;
    @FXML private TableColumn<SaleResponseDTO, String> totalColumn;
    @FXML private TableColumn<SaleResponseDTO, String> statusColumn;
    @FXML private TableColumn<SaleResponseDTO, String> dateColumn;
    @FXML private ComboBox<String> periodComboBox;
    @FXML private TextField searchField;
    @FXML private Label totalSalesLabel;
    @FXML private Label resultsCount;
    @FXML private Button deleteButton;
    @FXML private Button newSaleButton;
    @FXML private Button cancelButton;
    @FXML private Button exportPdfButton;
    @FXML private Button exportHtmlButton;
    @FXML private Button exportExcelButton;

    private final SaleService saleService = new SaleService();
    private final ObservableList<SaleResponseDTO> salesList = FXCollections.observableArrayList();
    private FilteredList<SaleResponseDTO> filteredSales;
    private final String token = SessionManager.getToken();
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTable();

        periodComboBox.setItems(FXCollections.observableArrayList(
                "Último Mês", "Últimos 3 Meses", "Últimos 6 Meses", "Último Ano"
        ));
        periodComboBox.getSelectionModel().selectFirst();

        filteredSales = new FilteredList<>(salesList, s -> true);
        salesTable.setItems(filteredSales);

        loadSales();
        loadTotalSales();

        periodComboBox.setOnAction(event -> loadTotalSales());
        deleteButton.setOnAction(event -> deleteSelectedSale());
        newSaleButton.setOnAction(event -> openNewSaleForm());
        cancelButton.setOnAction(event -> cancelSelectedSale());

        exportPdfButton.setOnAction(event -> exportSelectedSale("pdf"));
        exportHtmlButton.setOnAction(event -> exportSelectedSale("html"));
        exportExcelButton.setOnAction(event -> exportSelectedSale("xlsx"));

        searchField.textProperty().addListener((obs, oldText, newText) -> filterSales(newText));
    }

    private void setupTable() {
        clientColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClientName()));
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("pt", "MZ"));
        totalColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTotalAmount() != null ? formatter.format(c.getValue().getTotalAmount()) : formatter.format(0)
        ));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus() != null ? c.getValue().getStatus() : ""
        ));
        dateColumn.setCellValueFactory(c -> {
            String rawDate = c.getValue().getSaleDate();
            if (rawDate == null || rawDate.isBlank()) return new SimpleStringProperty("");
            try {
                LocalDateTime dateTime = LocalDateTime.parse(rawDate, DateTimeFormatter.ISO_DATE_TIME);
                return new SimpleStringProperty(dateTime.format(DISPLAY_FORMAT));
            } catch (DateTimeParseException e) {
                return new SimpleStringProperty(rawDate);
            }
        });
        productColumn.setCellValueFactory(c -> {
            if (c.getValue().getItems() != null && !c.getValue().getItems().isEmpty()) {
                return new SimpleStringProperty(c.getValue().getItems().get(0).getProductName());
            }
            return new SimpleStringProperty("");
        });
        codeColumn.setCellValueFactory(c -> {
            if (c.getValue().getItems() != null && !c.getValue().getItems().isEmpty()) {
                return new SimpleStringProperty(c.getValue().getItems().get(0).getProductCode());
            }
            return new SimpleStringProperty("");
        });
    }

    private void loadSales() {
        saleService.listSales(token)
                .thenAccept(sales -> Platform.runLater(() -> {
                    if (sales == null || sales.isEmpty()) {
                        salesList.clear();
                        AlertUtil.showInfo("Vendas", "Nenhuma venda encontrada.");
                    } else {
                        salesList.setAll(sales);
                        updateResultsCount();
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        AlertUtil.showError("Erro", "Falha ao carregar vendas: " + cause.getMessage());
                        cause.printStackTrace();
                    });
                    return null;
                });
    }

    private void loadTotalSales() {
        String period = mapPeriod(periodComboBox.getValue());
        saleService.getTotalSales(period, token)
                .thenAccept(total -> Platform.runLater(() -> {
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("pt", "MZ"));
                    totalSalesLabel.setText(formatter.format(total));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> totalSalesLabel.setText("Mzn 0,00"));
                    return null;
                });
    }

    private String mapPeriod(String period) {
        if (period == null) return "month";
        switch (period) {
            case "Último Mês": return "month";
            case "Últimos 3 Meses": return "3months";
            case "Últimos 6 Meses": return "6months";
            case "Último Ano": return "year";
            default: return "month";
        }
    }

    private void filterSales(String query) {
        if (query == null || query.isBlank()) {
            filteredSales.setPredicate(s -> true);
        } else {
            String lowerCase = query.toLowerCase();
            filteredSales.setPredicate(sale ->
                    sale.getClientName().toLowerCase().contains(lowerCase) ||
                            (sale.getSaleCode() != null && sale.getSaleCode().toLowerCase().contains(lowerCase))
            );
        }
        updateResultsCount();
    }

    private void updateResultsCount() {
        resultsCount.setText(filteredSales.size() + " vendas encontradas");
    }

    private void deleteSelectedSale() {
        SaleResponseDTO selected = salesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("Seleção necessária", "Por favor, selecione uma venda para deletar.");
            return;
        }
        boolean confirmed = AlertUtil.showConfirmation("Confirmar exclusão",
                "Deseja realmente deletar a venda: " + selected.getSaleCode() + "?");
        if (!confirmed) return;

        saleService.deleteSale(selected.getId(), token)
                .thenRun(() -> Platform.runLater(() -> {
                    salesList.remove(selected);
                    updateResultsCount();
                    loadTotalSales();
                    AlertUtil.showInfo("Sucesso", "Venda deletada com sucesso!");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        AlertUtil.showError("Erro", "Falha ao deletar venda: " + cause.getMessage());
                        cause.printStackTrace();
                    });
                    return null;
                });
    }

    @FXML
    private void cancelSelectedSale() {
        SaleResponseDTO selected = salesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("Atenção", "Selecione uma venda para cancelar.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Confirmação de cancelamento",
                "Deseja realmente cancelar a venda " + selected.getSaleCode() + "?");
        if (!confirmed) return;

        saleService.cancelSale(selected.getId(), token)
                .thenAccept(updatedSale -> Platform.runLater(() -> {
                    if (updatedSale != null) {
                        int index = salesList.indexOf(selected);
                        if (index >= 0) salesList.set(index, updatedSale);
                        AlertUtil.showInfo("Sucesso", "Venda cancelada com sucesso!");
                        loadTotalSales();
                    } else {
                        AlertUtil.showError("Erro", "Não foi possível cancelar a venda.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        AlertUtil.showError("Erro", "Falha ao cancelar venda: " + cause.getMessage());
                        cause.printStackTrace();
                    });
                    return null;
                });
    }

    private void openNewSaleForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SaleForm.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);

            SaleFormController formController = loader.getController();
            formController.setOnSaleCreated(() -> {
                loadSales();
                loadTotalSales();
            });

            stage.showAndWait();
        } catch (IOException e) {
            AlertUtil.showError("Erro", "Falha ao abrir formulário de venda: " + e.getMessage());
        }
    }

    // ================= EXPORT METHODS =================
    private void exportSelectedSale(String format) {
        SaleResponseDTO selected = salesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("Atenção", "Selecione uma venda para exportar.");
            return;
        }

        CompletableFuture<byte[]> exportFuture;

        switch (format.toLowerCase()) {
            case "pdf":
                exportFuture = saleService.exportSaleToPdf(selected.getId(), token);
                break;
            case "html":
                exportFuture = saleService.exportSaleToHtml(selected.getId(), token);
                break;
            case "xlsx":
            case "excel":
                exportFuture = saleService.exportSaleToExcel(selected.getId(), token);
                break;
            default:
                AlertUtil.showError("Erro", "Formato de exportação não suportado: " + format);
                return;
        }

        exportFuture.thenAccept(bytes -> {
            if (bytes == null || bytes.length == 0) {
                Platform.runLater(() -> AlertUtil.showError("Erro", "O servidor retornou um arquivo vazio."));
                return;
            }
            Platform.runLater(() -> saveFileToDownloads(bytes, format, selected.getId().toString()));
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                AlertUtil.showError("Erro", "Falha ao exportar venda: " + cause.getMessage());
                cause.printStackTrace();
            });
            return null;
        });
    }

    // Salva automaticamente na pasta Downloads
    private void saveFileToDownloads(byte[] data, String format, String saleCode) {
        new Thread(() -> {
            try {
                String userHome = System.getProperty("user.home");
                File downloadsFolder = new File(userHome, "Downloads");
                if (!downloadsFolder.exists()) downloadsFolder.mkdirs();
                Date now = new Date();
                File file = new File(downloadsFolder, "Venda_"+now.getSeconds()+ saleCode + "." + format);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(data);
                }

                Platform.runLater(() ->
                        AlertUtil.showInfo("Sucesso", "Arquivo exportado com sucesso em: " + file.getAbsolutePath())
                );
            } catch (IOException e) {
                Platform.runLater(() ->
                        AlertUtil.showError("Erro", "Falha ao salvar arquivo: " + e.getMessage())
                );
            }
        }).start();
    }
}
