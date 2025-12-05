package org.manager.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import org.manager.dto.WarehouseResponseDTO;
import org.manager.dto.WarehouseRequestDTO;
import org.manager.service.WarehouseService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

import java.util.List;

public class WarehouseController {

    private final WarehouseService warehouseService = new WarehouseService();
    private final String token = SessionManager.getToken();

    @FXML private TextField txtSearch;
    @FXML private TableView<WarehouseResponseDTO> warehouseTable;
    @FXML private TableColumn<WarehouseResponseDTO, Long> idColumn;
    @FXML private TableColumn<WarehouseResponseDTO, String> nameColumn;
    @FXML private TableColumn<WarehouseResponseDTO, String> locationColumn;
    @FXML private TableColumn<WarehouseResponseDTO, Integer> capacityColumn;
    @FXML private TableColumn<WarehouseResponseDTO, String> descriptionColumn;
    @FXML private TableColumn<WarehouseResponseDTO, String> statusColumn;
    @FXML private TableColumn<WarehouseResponseDTO, String> emailColumn;
    @FXML private TableColumn<WarehouseResponseDTO, String> phoneColumn;
    @FXML private TableColumn<WarehouseResponseDTO, String> managerColumn;
    @FXML private TableColumn<WarehouseResponseDTO, String> companyColumn;

    private final ObservableList<WarehouseResponseDTO> warehousesData = FXCollections.observableArrayList();
    private FilteredList<WarehouseResponseDTO> filteredData;
    private SortedList<WarehouseResponseDTO> sortedData;

    @FXML
    private void initialize() {
        setupTableColumns();
        warehouseTable.setItems(warehousesData);

        setupRealTimeSearch();
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterData(newVal));

        loadWarehouses();
    }

    // ===================== TABLE COLUMNS =====================
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusColumn.setCellValueFactory(cellData -> {
            boolean active = cellData.getValue().isActive();
            return new ReadOnlyStringWrapper(active ? "Ativo" : "Inativo");
        });
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        managerColumn.setCellValueFactory(new PropertyValueFactory<>("manager"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
    }

    // ===================== SEARCH =====================
    private void setupRealTimeSearch() {
        filteredData = new FilteredList<>(warehousesData, w -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(warehouseTable.comparatorProperty());
        warehouseTable.setItems(sortedData);
    }

  @FXML
  private void handleSearch(){
       filterData(txtSearch.getText()) ;
  }
    private void filterData(String search) {
        if (search == null || search.isEmpty()) {
            filteredData.setPredicate(w -> true);
        } else {
            String lower = search.toLowerCase().trim();
            filteredData.setPredicate(w -> w.getName().toLowerCase().contains(lower)
                    || w.getManager().toLowerCase().contains(lower)
                    || w.getEmail().toLowerCase().contains(lower)
                    || w.getPhone().toLowerCase().contains(lower)
                    || w.getLocation().toLowerCase().contains(lower)
                    || w.getDescription().toLowerCase().contains(lower));
        }
        warehouseTable.refresh();
    }

    // ===================== LOAD DATA =====================
    private void loadWarehouses() {
        warehouseService.getAllWarehouses(token)
                .thenAccept(list -> Platform.runLater(() -> {
                    if (list != null) warehousesData.setAll(list);
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    AlertUtil.showError("Erro", "Não foi possível carregar os armazéns.");
                    return null;
                });
    }

    public void refreshWarehouseTable() {
        loadWarehouses();
    }

    // ===================== CREATE / UPDATE =====================
    private void openWarehouseForm(boolean isEdit, WarehouseResponseDTO data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WarehouseForm.fxml"));
            Parent root = loader.load();
            WarehouseFormController controller = loader.getController();
            controller.setEditMode(isEdit);
            controller.setWarehouseController(this);

            if (isEdit && data != null) {
                controller.setEditingWarehouse(data);
                controller.changeModalTitle("Atualizar Armazém");
            } else {
                controller.changeModalTitle("Cadastro de Armazém");
            }

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erro", "Não foi possível abrir o formulário.");
        }
    }

    @FXML
    private void handleOpenFormCreateWarehouse() {
        openWarehouseForm(false, null);
    }

    @FXML
    private void handleOpenFormUpdateWarehouse() {
        WarehouseResponseDTO selected = warehouseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Alerta", "Selecione um armazém primeiro.");
            return;
        }
        openWarehouseForm(true, selected);
    }

    // ===================== DELETE =====================
    @FXML
    private void handleDeleteWarehouse() {
        WarehouseResponseDTO selected = warehouseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Erro", "Selecione um armazém.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja realmente excluir o armazém \"" + selected.getName() + "\"?");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                warehouseService.deleteWarehouse(selected.getId(), token)
                        .thenAccept(res -> Platform.runLater(() -> {
                            warehousesData.remove(selected);
                            AlertUtil.showInfo("Sucesso", "Armazém deletado!");
                        }))
                        .exceptionally(ex -> {
                            ex.printStackTrace();
                            AlertUtil.showError("Erro", "Não foi possível deletar o armazém.");
                            return null;
                        });
            }
        });
    }
}
