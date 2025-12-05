package org.manager.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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
import org.manager.dto.SaleDTO;
import org.manager.service.SaleService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;
import org.manager.util.FormatUtil;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class SaleController {

    @FXML private TableView<SaleDTO> salesTable;
    @FXML private TableColumn<SaleDTO, String> clientColumn;
    @FXML private TableColumn<SaleDTO,String>  codeColumn;
    @FXML private TableColumn<SaleDTO,String>  productColumn;
    @FXML private TableColumn<SaleDTO, String> totalColumn;
    @FXML private TableColumn<SaleDTO, String> statusColumn;
    @FXML private TableColumn<SaleDTO, String> dateColumn;
    @FXML private ComboBox<String> periodComboBox;
    @FXML private TextField searchField;
    @FXML private Label totalSalesLabel;
    @FXML private Label resultsCount;
    @FXML private Button deleteButton;
    @FXML private Button newSaleButton;

    private final SaleService saleService = new SaleService();
    private final ObservableList<SaleDTO> salesList = FXCollections.observableArrayList();
    private FilteredList<SaleDTO> filteredSales;
    private final String token = SessionManager.getToken();
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


    @FXML
    public void initialize() {
        setupTable();
        periodComboBox.getSelectionModel().select("Último Mês");
        filteredSales = new FilteredList<>(salesList, s -> true);
        salesTable.setItems(filteredSales);

        loadSales();
        loadTotalSales();

        periodComboBox.setOnAction(event -> loadTotalSales());
        deleteButton.setOnAction(event -> deleteSelectedSale());
        newSaleButton.setOnAction(event -> openNewSaleForm());

        // Pesquisa em tempo real
        searchField.textProperty().addListener((obs, oldText, newText) -> filterSales(newText));
    }

    private void setupTable() {
        clientColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClientName()));
        totalColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTotalAmount() != null ? String.format("Mzn %,d", c.getValue().getTotalAmount().longValue()) : "Mzn 0"));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus() != null ? c.getValue().getStatus() : ""));
        dateColumn.setCellValueFactory(c -> {
            String rawDate = c.getValue().getSaleDate();

            if (rawDate == null || rawDate.isBlank()) {
                return new SimpleStringProperty("");
            }

            try {
                LocalDateTime dateTime = LocalDateTime.parse(rawDate);
                return new SimpleStringProperty(dateTime.format(DISPLAY_FORMAT));
            } catch (Exception e) {
                return new SimpleStringProperty(rawDate); // mostra original se falhar
            }
        });

        productColumn.setCellValueFactory(c -> {
            if (c.getValue().getItems() != null && !c.getValue().getItems().isEmpty()) {
                return new SimpleStringProperty(
                        c.getValue().getItems().getFirst().getProductName()
                );
            }
            return new SimpleStringProperty("");
        });
        codeColumn.setCellValueFactory(c->{
            if (c.getValue().getItems()!=null && !c.getValue().getItems().isEmpty()){
                return new SimpleStringProperty(c.getValue().getItems().getFirst().getProductCode());
            }
            return new SimpleStringProperty("");
        });
    }

    // -------------------- CARREGAR VENDAS --------------------
    private void loadSales() {
        saleService.listSales(token)
                .thenAccept(sales -> Platform.runLater(() -> {
                    if (sales == null || sales.isEmpty()) {
                        AlertUtil.showInfo("Vendas", "Nenhuma venda encontrada.");
                        salesList.clear();
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
        Object selected = periodComboBox.getValue();
        // Converter para string segura
        String period = (selected == null) ? "" : selected.toString().trim();
        // Mapear opções do ComboBox → valores internos da API
        switch (period) {
            case "Último Mês":
                period = "month";
                break;

            case "Últimos 3 Meses":
                period = "3months";
                break;

            case "Últimos 6 Meses":
                period = "6months";
                break;

            case "Último Ano":
                period = "year";
                break;

            default:
                period = "month"; // fallback
                break;
        }

        saleService.getTotalSales(period, token)
                .thenAccept(total -> Platform.runLater(() -> {

                    NumberFormat formatter = NumberFormat.getInstance(new Locale("pt", "MZ"));
                    formatter.setMinimumFractionDigits(2);
                    formatter.setMaximumFractionDigits(2);

                    totalSalesLabel.setText("Mzn " + formatter.format(total));
                    System.out.println("Total de vendas: " + total);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> totalSalesLabel.setText("Mzn 0.00"));
                    return null;
                });
    }


    // -------------------- DELETAR VENDA --------------------
    private void deleteSelectedSale() {
        SaleDTO selected = salesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("Seleção necessária", "Por favor, selecione uma venda para deletar.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Confirmar exclusão",
                "Deseja realmente deletar a venda: " + selected.getSaleCode() + "?");
        if (!confirmed) return;

        saleService.deleteSale(selected.getId(), token)
                .thenRun(() -> Platform.runLater(() -> {
                    AlertUtil.showInfo("Sucesso", "Venda deletada com sucesso!");
                    salesList.remove(selected);
                    updateResultsCount();
                    loadTotalSales();
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

    // -------------------- ABRIR FORMULÁRIO DE NOVA VENDA --------------------
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

    // -------------------- FILTRAR PESQUISA --------------------
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
}
