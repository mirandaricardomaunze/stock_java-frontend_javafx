package org.manager.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.manager.dto.CompanyDTO;
import org.manager.dto.WarehouseRequestDTO;
import org.manager.dto.WarehouseResponseDTO;
import org.manager.service.CompanyService;
import org.manager.service.WarehouseService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

import static org.manager.util.SetupComboBoxDisplay.setupComboBoxDisplay;

/**
 * Controller para o formulário de criação/atualização de armazéns.
 * Funciona com JavaFX, valida campos obrigatórios e previne múltiplos armazéns principais por empresa.
 */
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
    @FXML private ComboBox<CompanyDTO> companyComboBox;
    @FXML private CheckBox activeCheckBox;
    @FXML private CheckBox principalCheckBox;

    private WarehouseResponseDTO editingWarehouse;        // Armazém em edição (null se criação)
    private WarehouseController warehouseController;      // Controller da tabela para refresh

    private final String token = SessionManager.getToken();
    private final ObservableList<CompanyDTO> companiesData = FXCollections.observableArrayList();

    private final WarehouseService warehouseService = new WarehouseService();
    private final CompanyService companyService = new CompanyService();

    @FXML
    private void initialize() {
        // Carrega empresas e configura display do ComboBox
        loadCompanies();
        setupComboBoxDisplay(companyComboBox, CompanyDTO::getName);
    }

    /** Define o controller da tabela para permitir refresh após criação/atualização */
    public void setWarehouseController(WarehouseController controller) {
        this.warehouseController = controller;
    }

    /** Altera o título do modal */
    public void changeModalTitle(String title) {
        if (title != null) titleModal.setText(title);
    }

    /** Define modo de edição ou criação */
    public void setEditMode(boolean isEditMode) {
        if (isEditMode) {
            titleModal.setText("Atualizar Armazém");
            btnUpdateAndCreate.setText("Atualizar");
        } else {
            titleModal.setText("Cadastrar Armazém");
            btnUpdateAndCreate.setText("Cadastrar");
        }
    }

    /** Cancela e fecha o formulário */
    @FXML
    private void handleCancel() {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        if (stage != null) stage.close();
    }

    /** Carrega lista de empresas do backend */
    @FXML
    private void loadCompanies() {
        companyService.getAllCompanies()
                .thenAccept(list -> Platform.runLater(() -> {
                    companiesData.setAll(list);
                    companyComboBox.setItems(companiesData);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Não foi possível carregar as empresas"));
                    return null;
                });
    }

    /** Botão de criar/atualizar */
    @FXML
    private void updateWarehouse() {
        if (btnUpdateAndCreate.getText().equalsIgnoreCase("Atualizar")) {
            updateWarehouseBackend();
        } else {
            createWarehouseBackend();
        }
    }

    /** Atualiza armazém existente */
    private void updateWarehouseBackend() {
        if (editingWarehouse == null) {
            AlertUtil.showError("Erro", "Nenhum armazém selecionado.");
            return;
        }
        if (!validateForm()) return;

        CompanyDTO selectedCompany = companyComboBox.getValue();
        if (selectedCompany == null) {
            AlertUtil.showError("Erro", "Selecione a empresa.");
            return;
        }

        // Valida se já existe outro armazém principal
        warehouseService.getWarehousesByCompany(selectedCompany.getId(), token)
                .thenAccept(list -> Platform.runLater(() -> {
                    boolean existsPrincipal = list.stream()
                            .anyMatch(w -> w.isPrincipal() && !w.getId().equals(editingWarehouse.getId()));

                    if (principalCheckBox.isSelected() && existsPrincipal) {
                        AlertUtil.showError("Erro", "Já existe um armazém principal para esta empresa.");
                        return;
                    }

                    WarehouseRequestDTO request = buildWarehouseForm();
                    if (request != null) {
                        warehouseService.updateWarehouse(editingWarehouse.getId(), request, token)
                                .thenAccept(res -> Platform.runLater(() -> {
                                    if (warehouseController != null) warehouseController.refreshWarehouseTable();
                                    AlertUtil.showInfo("Sucesso", "Armazém atualizado!");
                                    closeForm();
                                }))
                                .exceptionally(ex -> {
                                    Platform.runLater(() -> AlertUtil.showError("Erro", "Erro ao atualizar armazém."));
                                    return null;
                                });
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Erro ao validar armazém principal."));
                    return null;
                });
    }

    /** Cria novo armazém */
    private void createWarehouseBackend() {
        if (!validateForm()) return;

        CompanyDTO selectedCompany = companyComboBox.getValue();
        if (selectedCompany == null) {
            AlertUtil.showError("Erro", "Selecione a empresa.");
            return;
        }

        // Valida se já existe outro armazém principal
        warehouseService.getWarehousesByCompany(selectedCompany.getId(), token)
                .thenAccept(list -> Platform.runLater(() -> {
                    boolean existsPrincipal = list.stream().anyMatch(WarehouseResponseDTO::isPrincipal);

                    if (principalCheckBox.isSelected() && existsPrincipal) {
                        AlertUtil.showError("Erro", "Já existe um armazém principal para esta empresa.");
                        return;
                    }

                    WarehouseRequestDTO request = buildWarehouseForm();
                    if (request != null) {
                        warehouseService.createWarehouse(request, token)
                                .thenAccept(res -> Platform.runLater(() -> {
                                    if (warehouseController != null) warehouseController.refreshWarehouseTable();
                                    AlertUtil.showInfo("Sucesso", "Armazém criado!");
                                    closeForm();
                                }))
                                .exceptionally(ex -> {
                                    Platform.runLater(() -> AlertUtil.showError("Erro", "Erro ao criar armazém."));
                                    return null;
                                });
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Erro ao validar armazém principal."));
                    return null;
                });
    }

    /** Configura os campos para edição de armazém */
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

        companiesData.stream()
                .filter(c -> c.getId().equals(warehouse.getCompanyId()))
                .findFirst()
                .ifPresent(companyComboBox.getSelectionModel()::select);
    }

    /** Monta DTO do formulário */
    private WarehouseRequestDTO buildWarehouseForm() {
        try {
            CompanyDTO selectedCompany = companyComboBox.getValue();
            if (selectedCompany == null) return null;

            WarehouseRequestDTO warehouse = new WarehouseRequestDTO();
            warehouse.setName(nameField.getText().trim());
            warehouse.setPhone(phoneField.getText().trim());
            warehouse.setManager(managerField.getText().trim());
            warehouse.setEmail(emailField.getText().trim());
            warehouse.setDescription(descriptionField.getText().trim());
            warehouse.setLocation(locationField.getText().trim());
            warehouse.setCapacity(Integer.parseInt(capacityField.getText()));
            warehouse.setActive(activeCheckBox.isSelected());
            warehouse.setPrincipal(principalCheckBox.isSelected());
            warehouse.setCompanyId(selectedCompany.getId());

            return warehouse;
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erro", "Capacidade deve ser um número inteiro.");
            return null;
        }
    }

    /** Valida campos obrigatórios */
    private boolean validateForm() {
        if (nameField.getText().isEmpty() || locationField.getText().isEmpty()
                || capacityField.getText().isEmpty() || descriptionField.getText().isEmpty()
                || emailField.getText().isEmpty() || phoneField.getText().isEmpty()
                || managerField.getText().isEmpty()) {
            AlertUtil.showError("Erro", "Preencha todos os campos obrigatórios.");
            return false;
        }
        try {
            Integer.parseInt(capacityField.getText());
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erro", "Capacidade deve ser um número inteiro.");
            return false;
        }
        return true;
    }
}
