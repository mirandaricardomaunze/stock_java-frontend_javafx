package org.manager.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.manager.dto.*;
import org.manager.service.StockService;
import org.manager.service.ProductService;
import org.manager.service.WarehouseService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

public class StockFormController {

    @FXML private Label titleModal;
    @FXML private ComboBox<ProductResponseDTO> comboBoxProduct;
    @FXML private ComboBox<WarehouseResponseDTO> comboBoxWarehouse;
    @FXML private TextField txtQuantity;
    @FXML private Button btnUpdateAndCreate;

    private final StockService stockService = new StockService();
    private final ProductService productService = new ProductService();
    private final WarehouseService warehouseService = new WarehouseService();

    private final String token = SessionManager.getToken();
    private final Long companyId = SessionManager.getCurrentCompanyId();

    private final ObservableList<ProductResponseDTO> productsData = FXCollections.observableArrayList();
    private final ObservableList<WarehouseResponseDTO> warehousesData = FXCollections.observableArrayList();

    private StockController stockController;
    private StockResponseDTO editingStock;

    public void setStockController(StockController controller) {
        this.stockController = controller;
    }

    @FXML
    private void initialize() {
        setupComboDisplay(comboBoxProduct, ProductResponseDTO::getName);
        setupComboDisplay(comboBoxWarehouse, WarehouseResponseDTO::getName);

        loadProducts();
        loadWarehouses();
    }

    private <T> void setupComboDisplay(ComboBox<T> combo, java.util.function.Function<T, String> func) {
        combo.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : func.apply(item));
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : func.apply(item));
            }
        });
    }

    private void loadProducts() {
        productService.getAllProducts(token)
                .thenAccept(list -> Platform.runLater(() -> productsData.setAll(list)))
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
        comboBoxProduct.setItems(productsData);
    }

    private void loadWarehouses() {
        warehouseService.getActiveWarehousesByCompany(companyId,token)
                .thenAccept(list -> Platform.runLater(() -> warehousesData.setAll(list)))
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
        comboBoxWarehouse.setItems(warehousesData);
    }

    @FXML
    private void updateAndCreate() {
        if (editingStock != null) handleUpdate();
        else handleCreate();
    }

    private void handleCreate() {
        StockRequestDTO dto = buildRequest();
        if (dto == null) return;

        stockService.createOrUpdateAsync(dto, token)
                .thenAccept(saved -> Platform.runLater(() -> {
                    AlertUtil.showInfo("Sucesso", "Stock adicionado!");
                    if (stockController != null) stockController.refreshTable();
                    closeForm();
                }))
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private void handleUpdate() {
        StockRequestDTO dto = buildRequest();
        if (dto == null) return;

        stockService.createOrUpdateAsync(dto, token)
                .thenAccept(updated -> Platform.runLater(() -> {
                    AlertUtil.showInfo("Sucesso", "Stock atualizado!");
                    if (stockController != null) stockController.refreshTable();
                    closeForm();
                }))
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private StockRequestDTO buildRequest() {
        if (comboBoxProduct.getValue() == null || comboBoxWarehouse.getValue() == null) {
            AlertUtil.showError("Erro", "Selecione produto e armazém.");
            return null;
        }

        StockRequestDTO dto = new StockRequestDTO();
        if (editingStock != null) dto.setId(editingStock.getId());
        dto.setProductId(comboBoxProduct.getValue().getId());
        dto.setWarehouseId(comboBoxWarehouse.getValue().getId());
        dto.setQuantity(parseInt(txtQuantity.getText()));

        return dto;
    }

    public void setEditMode(boolean edit) {
        titleModal.setText(edit ? "Atualizar Stock" : "Adicionar Stock");
        btnUpdateAndCreate.setText(edit ? "Atualizar" : "Salvar");
    }

    public void populateForm(StockResponseDTO stock) {
        editingStock = stock;
        txtQuantity.setText(String.valueOf(stock.getQuantity()));
        comboBoxProduct.getSelectionModel().select(
                productsData.stream().filter(p -> p.getId().equals(stock.getProductId())).findFirst().orElse(null)
        );
        comboBoxWarehouse.getSelectionModel().select(
                warehousesData.stream().filter(w -> w.getId().equals(stock.getWarehouseId())).findFirst().orElse(null)
        );
    }

    private Integer parseInt(String txt) {
        try { return txt == null || txt.isBlank() ? null : Integer.parseInt(txt); }
        catch (Exception e) { return null; }
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
