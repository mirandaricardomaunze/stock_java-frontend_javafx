package org.manager.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.manager.dto.*;
import org.manager.enums.MovementOrigin;
import org.manager.enums.MovementStatusType;
import org.manager.enums.MovementType;
import org.manager.service.*;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductFormController {

    @FXML private Label titleModal;
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<CategoryDTO> comboBoxCategory;
    @FXML private TextField txtSku;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtSellingPrice;
    @FXML private TextField txtCostPrice;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtMinStock;
    @FXML private ComboBox<SupplierDTO> comboBoxSupplier;
    @FXML private TextField txtUnit;
    @FXML private TextField txtReference;
    @FXML private ComboBox<CompanyDTO> comboBoxCompany;
    @FXML private ComboBox<WarehouseResponseDTO> comboBoxWarehouse;
    @FXML private TextField txtBoxes;
    @FXML private TextField txtLocationCode;
    @FXML private TextField txtWeight;
    @FXML private TextField txtVolume;
    @FXML private DatePicker txtExpirationDate;
    @FXML private TextField txtBatchNumber;
    @FXML private TextField txtTaxPercentage;
    @FXML private CheckBox checkTaxIncluded;
    @FXML private TextField txtAccountingCode;
    @FXML private TextField txtBrand;
    @FXML private TextField txtModel;
    @FXML private TextField txtTags;
    @FXML private TextField txtImageUrl;
    @FXML private Button btnUpdateAndCreate;
    @FXML private TextField txtFullBoxes;
    @FXML private TextField txtRemainingItems;
    @FXML private TextField txtStockDetail;
    @FXML private TextField txtProfitMargin;
    @FXML private TextField txtProfitMarginPercentage;


    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final SupplierService supplierService = new SupplierService();
    private final WarehouseService warehouseService = new WarehouseService();
    private final CompanyService companyService = new CompanyService();
    private final MovementService movementService = new MovementService();

    private final String token = SessionManager.getToken();
    private final Long companyId = SessionManager.getCurrentCompanyId();
    private final Long userId = SessionManager.getCurrentUserId();
    private final String username = SessionManager.getCurrentUser();

    private ProductController productController;
    private ProductResponseDTO editingProduct;

    private final ObservableList<CategoryDTO> categoriesData = FXCollections.observableArrayList();
    private final ObservableList<SupplierDTO> suppliersData = FXCollections.observableArrayList();
    private final ObservableList<WarehouseResponseDTO> warehousesData = FXCollections.observableArrayList();
    private final ObservableList<CompanyDTO> companiesData = FXCollections.observableArrayList();

    public void setProductController(ProductController controller) {
        this.productController = controller;
    }

    @FXML
    private void initialize() {
        setupComboBoxDisplay(comboBoxCategory, CategoryDTO::getName);
        setupComboBoxDisplay(comboBoxSupplier, SupplierDTO::getName);
        setupComboBoxDisplay(comboBoxCompany, CompanyDTO::getName);
        setupComboBoxDisplay(comboBoxWarehouse, WarehouseResponseDTO::getName);

        loadCategories();
        loadSuppliers();
        loadCompanies();
        loadWarehouses();
    }

    private <T> void setupComboBoxDisplay(ComboBox<T> combo, java.util.function.Function<T, String> func) {
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

    // =================== LOAD ===================
    private void loadCategories() {
        categoryService.getAllCategories(token)
                .thenAccept(list -> Platform.runLater(() -> categoriesData.setAll(list)))
                .exceptionally(ex -> { ex.printStackTrace(); AlertUtil.showError("Erro", "Falha ao carregar categorias."); return null; });
        comboBoxCategory.setItems(categoriesData);
    }

    private void loadSuppliers() {
        supplierService.getAllSuppliers(token)
                .thenAccept(list -> Platform.runLater(() -> suppliersData.setAll(list)))
                .exceptionally(ex -> { ex.printStackTrace(); AlertUtil.showError("Erro", "Falha ao carregar fornecedores."); return null; });
        comboBoxSupplier.setItems(suppliersData);
    }

    private void loadCompanies() {
        companyService.getAllCompanies()
                .thenAccept(list -> Platform.runLater(() -> companiesData.setAll(list)))
                .exceptionally(ex -> { ex.printStackTrace(); AlertUtil.showError("Erro", "Falha ao carregar empresas."); return null; });
        comboBoxCompany.setItems(companiesData);
    }

    private void loadWarehouses() {
        warehouseService.getActiveWarehousesByCompany(companyId,token)
                .thenAccept(list -> Platform.runLater(() -> warehousesData.setAll(list)))
                .exceptionally(ex -> { ex.printStackTrace(); AlertUtil.showError("Erro", "Falha ao carregar armazéns."); return null; });
        comboBoxWarehouse.setItems(warehousesData);
    }

    // =================== CREATE / UPDATE ===================
    @FXML
    private void upadateAndCreate() {
        if (editingProduct != null) handleUpdate();
        else handleCreate();
    }

    private void handleCreate() {
        ProductRequestDTO request = buildProductRequest();
        if (request == null) return;

        productService.createProduct(request, token)
                .thenAccept(saved -> Platform.runLater(() -> {
                    AlertUtil.showInfo("Sucesso", "Produto criado!");
                    if (productController != null) productController.refreshTable();
                    createMovement(saved);
                    closeForm();
                }))
                .exceptionally(ex -> { ex.printStackTrace(); Platform.runLater(() -> AlertUtil.showError("Erro", "Erro ao criar.")); return null; });
    }

    private void handleUpdate() {
        ProductRequestDTO request = buildProductRequest();
        if (request == null) return;

        productService.updateProduct(editingProduct.getId(), request, token)
                .thenAccept(updated -> Platform.runLater(() -> {
                    AlertUtil.showInfo("Sucesso", "Produto atualizado!");
                    if (productController != null) productController.refreshTable();
                    createMovement(updated);
                    closeForm();
                }))
                .exceptionally(ex -> { ex.printStackTrace(); Platform.runLater(() -> AlertUtil.showError("Erro", "Erro ao atualizar.")); return null; });
    }

    // =================== BUILD REQUEST ===================
    private ProductRequestDTO buildProductRequest() {
        try {
            if (comboBoxCompany.getValue() == null || comboBoxCategory.getValue() == null ||
                    comboBoxSupplier.getValue() == null || comboBoxWarehouse.getValue() == null) {
                AlertUtil.showError("Erro", "Selecione empresa, categoria, fornecedor e armazém.");
                return null;
            }

            ProductRequestDTO dto = new ProductRequestDTO();

            // Básico
            dto.setName(txtName.getText());
            dto.setDescription(txtDescription.getText());
            dto.setSku(txtSku.getText());
            dto.setBarcode(txtBarcode.getText());
            dto.setReferenceNumber(txtReference.getText());
            dto.setUnitOfMeasure(txtUnit.getText());
            dto.setBoxes(parseInteger(txtBoxes.getText()));
            dto.setSellingPrice(parseBigDecimal(txtSellingPrice.getText()));
            dto.setCostPrice(parseBigDecimal(txtCostPrice.getText()));
            dto.setQuantityInStock(parseInteger(txtQuantity.getText()));
            dto.setMinimumStockLevel(parseInteger(txtMinStock.getText()));

            // Logística
            dto.setLocationCode(txtLocationCode.getText());
            dto.setWeight(parseDouble(txtWeight.getText()));
            dto.setVolume(parseDouble(txtVolume.getText()));
            dto.setExpirationDate(txtExpirationDate.getValue());
            dto.setBatchNumber(txtBatchNumber.getText());

            // Fiscal
            dto.setTaxPercentage(parseBigDecimal(txtTaxPercentage.getText()));
            dto.setIsTaxIncluded(checkTaxIncluded.isSelected());
            dto.setAccountingCode(txtAccountingCode.getText());

            // Catálogo
            dto.setBrand(txtBrand.getText());
            dto.setModel(txtModel.getText());
            dto.setTags(txtTags.getText());
            dto.setImageUrl(txtImageUrl.getText());

            // IDs
            dto.setCompanyId(comboBoxCompany.getValue().getId());
            dto.setWarehouseId(comboBoxWarehouse.getValue().getId());
            dto.setCategoryId(comboBoxCategory.getValue().getId());
            dto.setSupplierId(comboBoxSupplier.getValue().getId());

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erro", "Dados inválidos.");
            return null;
        }
    }

    // =================== MOVEMENT ===================
    private void createMovement(ProductResponseDTO p) {
        if (p == null) return;

        MovementRequestDTO dto = new MovementRequestDTO();
        dto.setProductId(p.getId());
        dto.setQuantity(p.getQuantityInStock());
        dto.setReferenceNumber(p.getReferenceNumber());
        dto.setType(MovementType.IN.name());
        dto.setOrigin(MovementOrigin.SYSTEM.name());
        dto.setStatus(MovementStatusType.COMPLETED.name());
        dto.setDescription(p.getName());
        dto.setCompanyId(p.getCompanyId());
        dto.setWarehouseId(p.getWarehouseId());
        dto.setUserId(String.valueOf(userId));
        dto.setUsername(username);
        dto.setDate(LocalDateTime.now());

        movementService.createAsync(dto, token);
    }

    // =================== EDIT MODE ===================
    public void setEditMode(boolean isEditMode) {
        btnUpdateAndCreate.setText(isEditMode ? "Atualizar" : "Cadastrar");
        titleModal.setText(isEditMode ? "Atualizar Produto" : "Cadastrar Produto");
    }

    public void populateForm(ProductResponseDTO p) {
        if (p == null) return;

        editingProduct = p;

        txtName.setText(p.getName());
        txtDescription.setText(p.getDescription());
        txtSku.setText(p.getSku());
        txtBarcode.setText(p.getBarcode());
        txtReference.setText(p.getReferenceNumber());
        txtUnit.setText(p.getUnitOfMeasure());
        txtBoxes.setText(toStr(p.getBoxes()));
        txtSellingPrice.setText(toStr(p.getSellingPrice()));
        txtCostPrice.setText(toStr(p.getCostPrice()));
        txtQuantity.setText(toStr(p.getQuantityInStock()));
        txtMinStock.setText(toStr(p.getMinimumStockLevel()));
        txtFullBoxes.setText(String.valueOf(p.getFullBoxes()));
        txtRemainingItems.setText(String.valueOf(p.getRemainingItems()));
        txtStockDetail.setText(p.getStockDetail());
        txtProfitMargin.setText(p.getProfitMargin().toString());
        txtProfitMarginPercentage.setText(p.getProfitMarginPercentage().toString() + " %");

        txtLocationCode.setText(p.getLocationCode());
        txtWeight.setText(toStr(p.getWeight()));
        txtVolume.setText(toStr(p.getVolume()));
        if (p.getExpirationDate() != null) txtExpirationDate.setValue(p.getExpirationDate());
        txtBatchNumber.setText(p.getBatchNumber());

        txtTaxPercentage.setText(toStr(p.getTaxPercentage()));
        checkTaxIncluded.setSelected(Boolean.TRUE.equals(p.getIsTaxIncluded()));
        txtAccountingCode.setText(p.getAccountingCode());

        txtBrand.setText(p.getBrand());
        txtModel.setText(p.getModel());
        txtTags.setText(p.getTags());
        txtImageUrl.setText(p.getImageUrl());
    }
    // =================== CANCEL / CLOSE ===================
    @FXML
    private void handleCancel() {
        closeForm();
    }

    // =================== UTIL ===================
    private String toStr(Object o) {
        return o == null ? "" : o.toString();
    }

    private Integer parseInteger(String txt) {
        try { return txt == null || txt.isBlank() ? null : Integer.parseInt(txt); }
        catch (Exception e) { return null; }
    }

    private BigDecimal parseBigDecimal(String txt) {
        try { return txt == null || txt.isBlank() ? null : new BigDecimal(txt); }
        catch (Exception e) { return null; }
    }

    private Double parseDouble(String txt) {
        try { return txt == null || txt.isBlank() ? null : Double.parseDouble(txt); }
        catch (Exception e) { return null; }
    }

    private void closeForm() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        if (stage != null) stage.close();
    }
}
