package org.manager.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.manager.dto.TransferResponseDTO;
import org.manager.service.TransferService;
import org.manager.service.WarehouseService;
import org.manager.service.ProductService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

import java.io.IOException;
import java.util.List;

public class TransferController {

    @FXML private TableView<TransferResponseDTO> transferTable;
    @FXML private TableColumn<TransferResponseDTO, Long> colId;
    @FXML private TableColumn<TransferResponseDTO, String> colProduct;
    @FXML private TableColumn<TransferResponseDTO, String> colSourceWarehouse;
    @FXML private TableColumn<TransferResponseDTO, String> colTargetWarehouse;
    @FXML private TableColumn<TransferResponseDTO, Integer> colQuantity;
    @FXML private TableColumn<TransferResponseDTO, String> colDate;
    @FXML private TableColumn<TransferResponseDTO, String> colUser;

    @FXML private TextField txtSearch;

    private final TransferService transferService = new TransferService();
    private final ProductService productService = new ProductService();
    private final WarehouseService warehouseService = new WarehouseService();

    private final ObservableList<TransferResponseDTO> allTransfersData = FXCollections.observableArrayList();
    private FilteredList<TransferResponseDTO> filteredData;
    private SortedList<TransferResponseDTO> sortedData;

    private final String token = SessionManager.getToken();

    @FXML
    private void initialize() {
        setupTable();
        loadTransfers();
        setupSearch();
    }

    private void setupTable() {
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colProduct.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductName()));
        colSourceWarehouse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSourceWarehouse()));
        colTargetWarehouse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDestinationWarehouse()));
        colQuantity.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTransferDate() != null ? data.getValue().getTransferDate().toString() : ""
        ));
        colUser.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUser() != null ? data.getValue().getUser() : ""
        ));
    }


    private void loadTransfers() {
        transferService.getAllAsync().thenAccept(list ->
                Platform.runLater(() -> allTransfersData.setAll(list))
        ).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao carregar transferências"));
            return null;
        });
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(allTransfersData, p -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(transferTable.comparatorProperty());
        transferTable.setItems(sortedData);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterTransfers(newVal));
    }

    private void filterTransfers(String filter) {
        if (filteredData == null) return;
        filteredData.setPredicate(tr -> {
            if (filter == null || filter.isEmpty()) return true;
            String lower = filter.toLowerCase();
            return tr.getProductName().toLowerCase().contains(lower)
                    || tr.getSourceWarehouse().toLowerCase().contains(lower)
                    || tr.getDestinationWarehouse().toLowerCase().contains(lower)
                    || tr.getId().toString().contains(lower);
        });
    }

    @FXML
    private void handleSearchTransfer() {
        filterTransfers(txtSearch.getText());
    }

    @FXML
    private void openTransferForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransferForm.fxml"));
            Parent root = loader.load();

            TransferFormController controller = loader.getController();
            controller.setTransferController(this);
            controller.setEditMode(false);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateOpenTransferForm() {
        TransferResponseDTO selected = transferTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Erro", "Selecione uma transferência.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransferForm.fxml"));
            Parent root = loader.load();

            TransferFormController controller = loader.getController();
            controller.setTransferController(this);
            controller.setEditMode(true);
            controller.populateForm(selected);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteTransfer() {
        TransferResponseDTO selected = transferTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Erro", "Selecione uma transferência para remover.");
            return;
        }
        boolean confirm = AlertUtil.showConfirmation("Confirmação", "Deseja realmente remover esta transferência?");
        if (!confirm) return;

        transferService.deleteAsync(selected.getId())
                .thenRun(() -> Platform.runLater(() -> {
                    allTransfersData.remove(selected);
                    AlertUtil.showInfo("Sucesso", "Transferência removida com sucesso!");
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao remover transferência"));
                    return null;
                });
    }

    @FXML
    private void handleExportTransfer() {
        // Aqui você pode implementar a exportação para Excel, CSV ou PDF
        AlertUtil.showInfo("Exportar", "Funcionalidade de exportação ainda não implementada.");
    }

    public void refreshTable() {
        loadTransfers();
    }
}
