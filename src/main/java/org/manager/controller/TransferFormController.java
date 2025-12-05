package org.manager.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.manager.dto.*;
import org.manager.service.TransferService;
import org.manager.service.WarehouseService;
import org.manager.service.ProductService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;
import org.manager.util.SetupComboBoxDisplay;

public class TransferFormController {
    @FXML private Label titleModal;
    @FXML private ComboBox<ProductResponseDTO> comboProduct;
    @FXML private ComboBox<WarehouseResponseDTO> comboSourceWarehouse;
    @FXML private ComboBox<WarehouseResponseDTO> comboTargetWarehouse;
    @FXML private TextField txtQuantity;
    @FXML private Button btnUpdateAndCreate;

    private final TransferService transferService = new TransferService();
    private final ProductService productService = new ProductService();
    private final WarehouseService warehouseService = new WarehouseService();
    private final String token = SessionManager.getToken();
    private final Long companyId = SessionManager.getCurrentCompanyId();
    private TransferController transferController;
    private TransferResponseDTO editingTransfer;

    private final ObservableList<ProductResponseDTO> productsData = FXCollections.observableArrayList();
    private final ObservableList<WarehouseResponseDTO> warehousesData = FXCollections.observableArrayList();

    public void setTransferController(TransferController controller) {
        this.transferController = controller;
    }

    @FXML
    private void initialize() {
        loadProducts();
        loadWarehouses();

        comboProduct.setItems(productsData);
        comboSourceWarehouse.setItems(warehousesData);
        comboTargetWarehouse.setItems(warehousesData);

        SetupComboBoxDisplay.setupComboBoxDisplay(comboProduct, ProductResponseDTO::getName);
        SetupComboBoxDisplay.setupComboBoxDisplay(comboSourceWarehouse, WarehouseResponseDTO::getName);
        SetupComboBoxDisplay.setupComboBoxDisplay(comboTargetWarehouse, WarehouseResponseDTO::getName);
    }

    private void loadProducts() {
        productService.getAllProducts(token)
                .thenAccept(list -> Platform.runLater(() -> productsData.setAll(list)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao carregar produtos: " + ex.getMessage()));
                    ex.printStackTrace();
                    return null;
                });
    }

    private void loadWarehouses() {
        warehouseService.getAllWarehouses(token)
                .thenAccept(list -> Platform.runLater(() -> warehousesData.setAll(list)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao carregar armazéns: " + ex.getMessage()));
                    ex.printStackTrace();
                    return null;
                });
    }

    public void setEditMode(boolean isEditMode) {
        btnUpdateAndCreate.setText(isEditMode ? "Atualizar" : "Cadastrar");
        titleModal.setText(isEditMode ? "Atualizar Transferência" : "Nova Transferência");
    }

    public void populateForm(TransferResponseDTO transfer) {
        if (transfer == null) return;
        editingTransfer = transfer;

        productsData.stream()
                .filter(p -> p.getId().equals(transfer.getProductId()))
                .findFirst()
                .ifPresent(comboProduct::setValue);

        warehousesData.stream()
                .filter(w -> w.getId().equals(transfer.getSourceWarehouseId()))
                .findFirst()
                .ifPresent(comboSourceWarehouse::setValue);

        warehousesData.stream()
                .filter(w -> w.getId().equals(transfer.getDestinationWarehouseId()))
                .findFirst()
                .ifPresent(comboTargetWarehouse::setValue);

        txtQuantity.setText(String.valueOf(transfer.getQuantity()));
    }

    @FXML
    private void updateAndCreate() {
        try {
            ProductResponseDTO product = comboProduct.getValue();
            WarehouseResponseDTO source = comboSourceWarehouse.getValue();
            WarehouseResponseDTO target = comboTargetWarehouse.getValue();
            int quantity = Integer.parseInt(txtQuantity.getText());

            if (product == null || source == null || target == null) {
                AlertUtil.showError("Erro", "Selecione produto e armazéns.");
                return;
            }

            if (quantity <= 0) {
                AlertUtil.showError("Erro", "A quantidade deve ser maior que zero.");
                return;
            }

            TranferRequestDTO dto = new TranferRequestDTO();
            dto.setProductId(product.getId());
            dto.setSourceWarehouseId(source.getId());
            dto.setDestinationWarehouseId(target.getId());
            dto.setQuantity(quantity);
            dto.setCompanyId(companyId);

            if (editingTransfer != null) {
                transferService.updateAsync(editingTransfer.getId(), dto)
                        .thenAccept(t -> Platform.runLater(() -> {
                            AlertUtil.showInfo("Sucesso", "Transferência atualizada!");
                            if (transferController != null) transferController.refreshTable();
                            closeForm();
                        })).exceptionally(ex -> {
                            Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao atualizar transferência: " + ex.getMessage()));
                            ex.printStackTrace();
                            return null;
                        });
            } else {
                transferService.createAsync(dto)
                        .thenAccept(t -> Platform.runLater(() -> {
                            AlertUtil.showInfo("Sucesso", "Transferência criada!");
                            if (transferController != null) transferController.refreshTable();
                            closeForm();
                        })).exceptionally(ex -> {
                            String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                            Platform.runLater(() -> AlertUtil.showError("Erro",  msg));
                            ex.printStackTrace();
                            return null;
                        });
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erro", "Quantidade inválida.");
        }
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) txtQuantity.getScene().getWindow();
        if (stage != null) stage.close();
    }

}
