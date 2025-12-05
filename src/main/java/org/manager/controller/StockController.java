package org.manager.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.manager.dto.StockDTO;
import org.manager.service.StockService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

import java.io.IOException;

public class StockController {

    @FXML private TableView<StockDTO> stockTable;

    // COLUNAS
    @FXML private TableColumn<StockDTO, Long> colId;
    @FXML private TableColumn<StockDTO, String> colProduct;
    @FXML private TableColumn<StockDTO, String> colWarehouse;
    @FXML private TableColumn<StockDTO, Integer> colQuantity;

    @FXML private TextField txtSearch;

    private final StockService stockService = new StockService();
    private final String token = SessionManager.getToken();

    private final ObservableList<StockDTO> allStockData = FXCollections.observableArrayList();
    private FilteredList<StockDTO> filteredData;
    private SortedList<StockDTO> sortedData;

    @FXML
    private void initialize() {
        setupTable();
        loadStock();
        setupSearch();
    }

    // ============================
    // CONFIGURAÇÃO DA TABELA
    // ============================
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }

    // ============================
    // CARREGAR STOCK
    // ============================
    private void loadStock() {
        stockService.getAllAsync(token)
                .thenAccept(list -> Platform.runLater(() -> allStockData.setAll(list)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao carregar stock."));
                    ex.printStackTrace();
                    return null;
                });
    }

    // ============================
    // PESQUISA
    // ============================
    private void setupSearch() {
        filteredData = new FilteredList<>(allStockData, s -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(stockTable.comparatorProperty());
        stockTable.setItems(sortedData);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterStock(newVal));
    }

    private void filterStock(String filter) {
        if (filter == null || filter.isBlank()) {
            filteredData.setPredicate(s -> true);
            return;
        }

        String lower = filter.toLowerCase();
        filteredData.setPredicate(st ->
                st.getProductName().toLowerCase().contains(lower)
                        || st.getWarehouseName().toLowerCase().contains(lower)
                        || String.valueOf(st.getQuantity()).contains(lower)
        );
    }

    @FXML
    private void handleSearchStock() {
        filterStock(txtSearch.getText());
    }

    // ============================
    // ABRIR FORMULÁRIO NOVO
    // ============================
    @FXML
    private void openStockForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StockForm.fxml"));
            Parent root = loader.load();

            StockFormController controller = loader.getController();
            controller.setStockController(this);
            controller.setEditMode(false);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erro", "Não foi possível abrir o formulário de stock.");
        }
    }

    // ============================
    // ABRIR FORMULÁRIO PARA ATUALIZAR
    // ============================
    @FXML
    private void updateOpenStockForm() {
        StockDTO selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Erro", "Selecione um item de stock.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StockForm.fxml"));
            Parent root = loader.load();

            StockFormController controller = loader.getController();
            controller.setStockController(this);
            controller.setEditMode(true);
            controller.populateForm(selected);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erro", "Não foi possível abrir o formulário de stock.");
        }
    }

    // ============================
    // DELETAR STOCK
    // ============================
    @FXML
    private void handleDeleteStock() {
        StockDTO selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Erro", "Selecione um item de stock para remover.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText("Deseja realmente remover o stock selecionado?");
        alert.setContentText("Produto: " + selected.getProductName() +
                "\nArmazém: " + selected.getWarehouseName() +
                "\nQuantidade: " + selected.getQuantity());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                stockService.deleteAsync(selected.getId(), token)
                        .thenAccept(deleted -> Platform.runLater(() -> {
                            if (deleted) {
                                AlertUtil.showInfo("Sucesso", "Stock removido com sucesso.");
                                refreshTable();
                            } else {
                                AlertUtil.showError("Erro", "Falha ao remover stock.");
                            }
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao remover stock."));
                            ex.printStackTrace();
                            return null;
                        });
            }
        });
    }

    // ============================
    // ATUALIZAR TABELA
    // ============================
    public void refreshTable() {
        loadStock();
    }
}
