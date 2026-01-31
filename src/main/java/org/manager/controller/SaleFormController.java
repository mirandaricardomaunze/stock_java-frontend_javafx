package org.manager.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.manager.dto.*;
import org.manager.enums.PaymentMethod;
import org.manager.service.ProductService;
import org.manager.service.SaleService;
import org.manager.service.WarehouseService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;
import org.manager.util.SetupComboBoxDisplay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class SaleFormController {

    @FXML private TextField clientNameField;
    @FXML private ComboBox<PaymentMethod> paymentMethodComboBox;
    @FXML private ComboBox<WarehouseResponseDTO> warehouseComboBox;
    @FXML private TextField discountField;
    @FXML private TextField amountPaidField;

    @FXML private ComboBox<ProductResponseDTO> productComboBox;
    @FXML private TextField quantityField;
    @FXML private Button addItemButton;
    @FXML private Button removeItemButton;
    @FXML private Button submitButton;

    @FXML private TableView<SaleItemRequestDTO> itemsTable;
    @FXML private TableColumn<SaleItemRequestDTO, String> productColumn;
    @FXML private TableColumn<SaleItemRequestDTO, Integer> quantityColumn;
    @FXML private TableColumn<SaleItemRequestDTO, BigDecimal> priceColumn;
    @FXML private TableColumn<SaleItemRequestDTO, BigDecimal> taxColumn;
    @FXML private TableColumn<SaleItemRequestDTO, BigDecimal> subtotalColumn;

    @FXML private Label totalLabel;
    @FXML private Label changeLabel;

    private final SaleService saleService = new SaleService();
    private final ProductService productService = new ProductService();
    private final WarehouseService warehouseService = new WarehouseService();
    private final String token = SessionManager.getToken();
    private final Long companyId = SessionManager.getCurrentCompanyId();
    private final Long userId = SessionManager.getCurrentUserId();

    private final ObservableList<SaleItemRequestDTO> itemsList = FXCollections.observableArrayList();
    private final ObservableList<ProductResponseDTO> productList = FXCollections.observableArrayList();
    private final ObservableList<WarehouseResponseDTO> warehouseList = FXCollections.observableArrayList();

    private Runnable onSaleCreated;

    public void setOnSaleCreated(Runnable callback) {
        this.onSaleCreated = callback;
    }

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTable();
        loadProducts();
        loadWarehouses();
        bindActions();
    }

    private void setupComboBoxes() {
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentMethodComboBox.setValue(PaymentMethod.CASH);

        SetupComboBoxDisplay.setupComboBoxDisplay(productComboBox, ProductResponseDTO::getName);
        SetupComboBoxDisplay.setupComboBoxDisplay(warehouseComboBox, WarehouseResponseDTO::getName);
    }

    private void setupTable() {
        itemsTable.setItems(itemsList);
        productColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getProductName()));
        quantityColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getQuantity()));
        priceColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getUnitPrice()));
        taxColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getTaxAmount()));
        subtotalColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getSubtotalWithTax()));
    }

    private void bindActions() {
        addItemButton.setOnAction(e -> addItem());
        removeItemButton.setOnAction(e -> removeSelectedItem());
        submitButton.setOnAction(e -> submitSale());

        discountField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalAndChange());
        amountPaidField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalAndChange());
    }

    private void loadProducts() {
        productService.getAllProducts(token)
                .thenAccept(products -> Platform.runLater(() -> productList.setAll(products)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao carregar produtos."));
                    ex.printStackTrace();
                    return null;
                });
        productComboBox.setItems(productList);
    }

    private void loadWarehouses() {
        warehouseService.getActiveWarehousesByCompany(companyId, token)
                .thenAccept(warehouses -> Platform.runLater(() -> warehouseList.setAll(warehouses)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao carregar armazéns."));
                    ex.printStackTrace();
                    return null;
                });
        warehouseComboBox.setItems(warehouseList);
    }

    @FXML
    private void addItem() {
        ProductResponseDTO selectedProduct = productComboBox.getValue();
        if (selectedProduct == null) {
            AlertUtil.showError("Erro", "Selecione um produto.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erro", "Quantidade inválida.");
            return;
        }

        BigDecimal unitPrice = selectedProduct.getSellingPrice() != null ? selectedProduct.getSellingPrice() : BigDecimal.ZERO;
        BigDecimal taxPercentage = selectedProduct.getTaxPercentage() != null ? selectedProduct.getTaxPercentage() : BigDecimal.ZERO;
        boolean taxIncluded = Boolean.TRUE.equals(selectedProduct.getIsTaxIncluded());

        BigDecimal subtotalWithoutTax = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal taxAmount;
        BigDecimal subtotal;

        if (taxIncluded) {
            taxAmount = BigDecimal.ZERO;
            subtotal = subtotalWithoutTax.setScale(2, RoundingMode.HALF_UP);
        } else {
            taxAmount = subtotalWithoutTax.multiply(taxPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            subtotal = subtotalWithoutTax.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        }

        SaleItemRequestDTO item = SaleItemRequestDTO.builder()
                .productId(selectedProduct.getId())
                .productName(selectedProduct.getName())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .taxAmount(taxAmount)
                .subtotal(subtotalWithoutTax) // subtotal sem IVA
                .build();

        itemsList.add(item);
        itemsTable.refresh(); // força atualização da tabela
        quantityField.clear();
        productComboBox.setValue(null);
        updateTotalAndChange();
    }

    @FXML
    private void removeSelectedItem() {
        SaleItemRequestDTO selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            itemsList.remove(selected);
            itemsTable.refresh(); // força atualização
            updateTotalAndChange();
        }
    }

    private void updateTotalAndChange() {
        BigDecimal total = itemsList.stream()
                .map(SaleItemRequestDTO::getSubtotalWithTax)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = parseBigDecimal(discountField.getText());
        total = total.subtract(discount).max(BigDecimal.ZERO);

        totalLabel.setText(total.setScale(2, RoundingMode.HALF_UP).toString());

        BigDecimal amountPaid = parseBigDecimal(amountPaidField.getText());
        BigDecimal change = amountPaid.subtract(total).max(BigDecimal.ZERO);
        changeLabel.setText(change.setScale(2, RoundingMode.HALF_UP).toString());
    }

    @FXML
    private void submitSale() {
        if (itemsList.isEmpty()) {
            AlertUtil.showError("Erro", "Adicione pelo menos um item à venda.");
            return;
        }

        if (warehouseComboBox.getValue() == null) {
            AlertUtil.showError("Erro", "Selecione o armazém da venda.");
            return;
        }

        BigDecimal totalAfterDiscount = itemsList.stream()
                .map(SaleItemRequestDTO::getSubtotalWithTax)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(parseBigDecimal(discountField.getText()))
                .max(BigDecimal.ZERO);

        BigDecimal amountPaid = parseBigDecimal(amountPaidField.getText());
        if (amountPaid.compareTo(totalAfterDiscount) < 0) {
            AlertUtil.showError("Pagamento insuficiente",
                    "O valor pago (" + amountPaid + ") é menor que o total (" + totalAfterDiscount + ").");
            return;
        }

        SaleRequestDTO saleRequest = SaleRequestDTO.builder()
                .clientName(clientNameField.getText())
                .paymentMethod(paymentMethodComboBox.getValue())
                .discount(parseBigDecimal(discountField.getText()))
                .amountPaid(amountPaid)
                .items(new ArrayList<>(itemsList)) // já com subtotais e IVA
                .companyId(companyId)
                .warehouseId(warehouseComboBox.getValue().getId())
                .userId(userId)                      // ✅ adicionar userId
                .userName(SessionManager.getCurrentUser())
                .build();

        CompletableFuture<SaleResponseDTO> future = saleService.createSale(saleRequest, token,userId);
        future.thenRun(() -> Platform.runLater(() -> {
            AlertUtil.showInfo("Sucesso", "Venda criada com sucesso!");
            if (onSaleCreated != null) onSaleCreated.run();
            clearForm();
            submitButton.getScene().getWindow().hide();
        })).exceptionally(ex -> {
            Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao criar venda: " + ex.getMessage()));
            ex.printStackTrace();
            return null;
        });
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) clientNameField.getScene().getWindow();
        if (stage != null) stage.close();
    }

    private void clearForm() {
        clientNameField.clear();
        discountField.clear();
        amountPaidField.clear();
        itemsList.clear();
        itemsTable.refresh();
        totalLabel.setText("0.00");
        changeLabel.setText("0.00");
        paymentMethodComboBox.setValue(PaymentMethod.CASH);
        warehouseComboBox.setValue(null);
        productComboBox.setValue(null);
        quantityField.clear();
    }

    private BigDecimal parseBigDecimal(String text) {
        try {
            return (text == null || text.isBlank()) ? BigDecimal.ZERO : new BigDecimal(text);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
