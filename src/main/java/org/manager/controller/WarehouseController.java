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
import org.manager.service.WarehouseService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

public class WarehouseController {

    private final WarehouseService warehouseService = new WarehouseService();
    private final String token = SessionManager.getToken();
    private final Long companyId = SessionManager.getCurrentCompanyId(); // 🔥 MULTIEMPRESA

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

    private final ObservableList<WarehouseResponseDTO> masterData = FXCollections.observableArrayList();
    private FilteredList<WarehouseResponseDTO> filteredData;

    @FXML
    private void initialize() {
        setupColumns();
        setupSearch();
        loadWarehousesByCompany();
    }

    // ===================== COLUNAS =====================
    private void setupColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        managerColumn.setCellValueFactory(new PropertyValueFactory<>("manager"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));

        statusColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().isActive() ? "Ativo" : "Inativo")
        );
    }

    // ===================== PESQUISA =====================
    private void setupSearch() {
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<WarehouseResponseDTO> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(warehouseTable.comparatorProperty());
        warehouseTable.setItems(sortedData);

        txtSearch.textProperty().addListener((obs, oldV, newV) -> applyFilter(newV));
    }

    private void applyFilter(String search) {
        filteredData.setPredicate(w -> {
            if (search == null || search.isBlank()) return true;
            String s = search.toLowerCase();

            return w.getName().toLowerCase().contains(s)
                    || safe(w.getLocation()).contains(s)
                    || safe(w.getManager()).contains(s)
                    || safe(w.getEmail()).contains(s)
                    || safe(w.getPhone()).contains(s);
        });
    }

    private String safe(String v) {
        return v == null ? "" : v.toLowerCase();
    }

    // ===================== LOAD =====================
    private void loadWarehousesByCompany() {
        warehouseService.getActiveWarehousesByCompany(companyId, token)
                .thenAccept(list -> Platform.runLater(() -> {
                    masterData.setAll(list);
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Falha ao carregar armazéns da empresa.")
                    );
                    return null;
                });
    }

    public void refreshWarehouseTable() {
        loadWarehousesByCompany();
    }

    // ===================== FORM =====================
    @FXML
    private void handleOpenCreate() {
        openForm(false, null);
    }

    @FXML
    private void handleOpenEdit() {
        WarehouseResponseDTO selected = warehouseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Aviso", "Selecione um armazém.");
            return;
        }
        openForm(true, selected);
    }

    private void openForm(boolean edit, WarehouseResponseDTO data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WarehouseForm.fxml"));
            Parent root = loader.load();

            WarehouseFormController controller = loader.getController();
            controller.setEditMode(edit);
            controller.setWarehouseController(this);

            if (edit) controller.setEditingWarehouse(data);

            Stage stage = new Stage(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erro", "Erro ao abrir formulário.");
        }
    }

    // ===================== DELETE =====================
    @FXML
    private void handleDelete() {
        WarehouseResponseDTO selected = warehouseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Erro", "Selecione um armazém.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja excluir o armazém \"" + selected.getName() + "\"?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                warehouseService.deleteWarehouse(selected.getId(), token)
                        .thenAccept(r -> Platform.runLater(() -> {
                            masterData.remove(selected);
                            AlertUtil.showInfo("Sucesso", "Armazém removido.");
                        }))
                        .exceptionally(ex -> {
                            ex.printStackTrace();
                            Platform.runLater(() ->
                                    AlertUtil.showError("Erro", "Falha ao excluir.")
                            );
                            return null;
                        });
            }
        });
    }
}
