package org.manager.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.manager.dto.WarehouseRequestDTO;
import org.manager.dto.WarehouseResponseDTO;
import org.manager.service.WarehouseService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

public class WarehouseFormController {

    @FXML private Button btnUpdateAndCreate;
    @FXML private Label titleModal;
    @FXML private TextField nameField;
    @FXML private TextField capacityField;
    @FXML private TextField descriptionField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField managerField;
    @FXML private TextField locationField;
    @FXML private CheckBox activeCheckBox;
    @FXML private CheckBox principalCheckBox;

    private WarehouseResponseDTO editingWarehouse;
    private WarehouseController warehouseController;

    private final WarehouseService warehouseService = new WarehouseService();
    private final String token = SessionManager.getToken();
    private final Long companyId = SessionManager.getCurrentCompanyId(); // 🔐 SEGURANÇA

    @FXML
    private void initialize() {
        // nada a carregar
    }

    public void setWarehouseController(WarehouseController controller) {
        this.warehouseController = controller;
    }

    public void setEditMode(boolean edit) {
        titleModal.setText(edit ? "Atualizar Armazém" : "Cadastrar Armazém");
        btnUpdateAndCreate.setText(edit ? "Atualizar" : "Cadastrar");
    }

    public void setEditingWarehouse(WarehouseResponseDTO warehouse) {
        this.editingWarehouse = warehouse;

        nameField.setText(warehouse.getName());
        phoneField.setText(warehouse.getPhone());
        managerField.setText(warehouse.getManager());
        emailField.setText(warehouse.getEmail());
        descriptionField.setText(warehouse.getDescription());
        locationField.setText(warehouse.getLocation());
        capacityField.setText(String.valueOf(warehouse.getCapacity()));
        activeCheckBox.setSelected(warehouse.isActive());
        principalCheckBox.setSelected(warehouse.isPrincipal());
    }

    @FXML
    private void handleCancel() {
        close();
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;

        warehouseService.getActiveWarehousesByCompany(companyId, token)
                .thenAccept(list -> Platform.runLater(() -> {

                    boolean existsPrincipal = list.stream()
                            .anyMatch(w ->
                                    w.isPrincipal()
                                            && (editingWarehouse == null
                                            || !w.getId().equals(editingWarehouse.getId()))
                            );

                    if (principalCheckBox.isSelected() && existsPrincipal) {
                        AlertUtil.showError("Erro", "Já existe um armazém principal para esta empresa.");
                        return;
                    }

                    WarehouseRequestDTO request = buildRequest();

                    if (editingWarehouse == null) {
                        create(request);
                    } else {
                        update(request);
                    }
                }));
    }

    private void create(WarehouseRequestDTO request) {
        warehouseService.createWarehouse(request, token)
                .thenAccept(r -> Platform.runLater(() -> {
                    success("Armazém criado com sucesso!");
                }))
                .exceptionally(ex -> error("Erro ao criar armazém."));
    }

    private void update(WarehouseRequestDTO request) {
        warehouseService.updateWarehouse(editingWarehouse.getId(), request, token)
                .thenAccept(r -> Platform.runLater(() -> {
                    success("Armazém atualizado com sucesso!");
                }))
                .exceptionally(ex -> error("Erro ao atualizar armazém."));
    }

    private WarehouseRequestDTO buildRequest() {
        WarehouseRequestDTO dto = new WarehouseRequestDTO();
        dto.setName(nameField.getText().trim());
        dto.setPhone(phoneField.getText().trim());
        dto.setManager(managerField.getText().trim());
        dto.setEmail(emailField.getText().trim());
        dto.setDescription(descriptionField.getText().trim());
        dto.setLocation(locationField.getText().trim());
        dto.setCapacity(Integer.parseInt(capacityField.getText()));
        dto.setActive(activeCheckBox.isSelected());
        dto.setPrincipal(principalCheckBox.isSelected());
        dto.setCompanyId(companyId); // 🔐 FIXO
        return dto;
    }

    private boolean validateForm() {
        if (nameField.getText().isBlank()
                || capacityField.getText().isBlank()
                || locationField.getText().isBlank()) {
            AlertUtil.showError("Erro", "Preencha os campos obrigatórios.");
            return false;
        }
        try {
            Integer.parseInt(capacityField.getText());
            return true;
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erro", "Capacidade inválida.");
            return false;
        }
    }

    private void success(String msg) {
        if (warehouseController != null) warehouseController.refreshWarehouseTable();
        AlertUtil.showInfo("Sucesso", msg);
        close();
    }

    private Void error(String msg) {
        Platform.runLater(() -> AlertUtil.showError("Erro", msg));
        return null;
    }

    private void close() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
