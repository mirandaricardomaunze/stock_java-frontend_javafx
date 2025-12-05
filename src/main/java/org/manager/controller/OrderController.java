package org.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.manager.mapper.OrderMapper;
import org.manager.model.*;
import org.manager.service.OrderService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

import java.math.BigDecimal;
import java.util.ArrayList;

public class OrderController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox formContainer;

    @FXML private TextField nameField, nuitField, phoneField, emailField, paymentField, addressField;
    @FXML private TextArea notesArea;
    @FXML private ComboBox<Company> companyComboBox;
    @FXML private ComboBox<Warehouse> warehouseComboBox;
    @FXML private ComboBox<Product> productComboBox;
    @FXML private TextField quantityField;
    @FXML private Button addProductButton, submitButton;

    @FXML private TableView<OrderItem> tableView;
    @FXML private TableColumn<OrderItem, String> colProduct;
    @FXML private TableColumn<OrderItem, Integer> colQuantity;
    @FXML private TableColumn<OrderItem, BigDecimal> colUnitPrice;
    @FXML private TableColumn<OrderItem, BigDecimal> colTotalPrice;
    @FXML private TableColumn<OrderItem, Void> colAction;
    @FXML private Label totalLabel;

    private final ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
    private final ObservableList<Company> companiesData = FXCollections.observableArrayList();
    private final ObservableList<Warehouse> warehousesData = FXCollections.observableArrayList();
    private final ObservableList<Product> productsData = FXCollections.observableArrayList();

    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final String token = SessionManager.getToken();

    private Order currentOrder;

    @FXML
    public void initialize() {
        currentOrder = new Order();
        currentOrder.setStatus("PENDING");
        currentOrder.setOrderItems(new ArrayList<>());
        setupTableView();
        setupButtons();
        setupComboBoxes();
        loadBackendData();
    }

    public OrderController() {
        this.orderService = new OrderService();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private void setupComboBoxes() {
        companyComboBox.setItems(companiesData);
        warehouseComboBox.setItems(warehousesData);
        productComboBox.setItems(productsData);

        // Company ComboBox
        companyComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Company item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        companyComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Company item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        // Warehouse ComboBox
        warehouseComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        warehouseComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        // Product ComboBox
        productComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null :
                        item.getName() + " (Preço: " + item.getSellingPrice() + ")");
            }
        });
        productComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null :
                        item.getName() + " (Preço: " + item.getSellingPrice() + ")");
            }
        });
    }

    private void setupTableView() {
        colProduct.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getProductName()));
        colQuantity.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        colUnitPrice.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getUnitPrice()));
        colTotalPrice.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTotalPrice()));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button removeBtn = new Button();
            private final FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);

            {
                // 🗑️ Ícone configurado diretamente
                deleteIcon.setGlyphSize(14);
                deleteIcon.setFill(javafx.scene.paint.Color.web("#d32f2f")); // vermelho suave

                // 🧩 Botão transparente
                removeBtn.setGraphic(deleteIcon);
                removeBtn.getStyleClass().add("icon-button");
                removeBtn.setPrefWidth(32);
                removeBtn.setPrefHeight(28);
                removeBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                removeBtn.setFocusTraversable(false);
                removeBtn.setBackground(null); // transparente total

                // Ação
                removeBtn.setOnAction(event -> {
                    OrderItem item = getTableView().getItems().get(getIndex());
                    orderItems.remove(item);
                    currentOrder.getOrderItems().remove(item);
                    updateTotal();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeBtn);
                }
            }
        });


        tableView.setItems(orderItems);
    }

    private void setupButtons() {
        addProductButton.setOnAction(e -> addProduct());
        submitButton.setOnAction(e -> submitOrder());
    }

    private void addProduct() {
        Product product = productComboBox.getSelectionModel().getSelectedItem();
        if (product == null) {
            AlertUtil.showError("Atenção", "Selecione um produto!");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(quantityField.getText());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            AlertUtil.showError("Atenção", "Quantidade inválida!");
            return;
        }

        BigDecimal unitPrice = product.getSellingPrice();
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(qty));

        OrderItem item = OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(qty)
                .unitPrice(unitPrice)
                .totalPrice(total)
                .build();

        orderItems.add(item);
        currentOrder.getOrderItems().add(item);
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal total = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText("Total: " + total);
        currentOrder.setTotalAmount(total);
    }

    private void submitOrder() {
        if (orderItems.isEmpty()) {
            AlertUtil.showError("Atenção", "Adicione pelo menos um produto!");
            return;
        }
        if (!isValidEmail(emailField.getText())) {
            AlertUtil.showError("Atenção", "Email inválido!");
            return;
        }
        System.out.println(" Endereço: "+addressField.getText());
        // Preencher dados do pedido
        currentOrder.setCustomerName(nameField.getText());
        currentOrder.setCustomerEmail(emailField.getText());
        currentOrder.setCustomerContact(phoneField.getText());
        currentOrder.setCustomerNuit(nuitField.getText());
        currentOrder.setPaymentMethod(paymentField.getText());
        currentOrder.setDeliveryAddress(addressField.getText());
        currentOrder.setNotes(notesArea.getText());

        Company company = companyComboBox.getSelectionModel().getSelectedItem();
        Warehouse warehouse = warehouseComboBox.getSelectionModel().getSelectedItem();

        if (company == null || warehouse == null) {
            AlertUtil.showError("Atenção", "Selecione empresa e armazém!");
            return;
        }

        // IDs e nomes para backend
        currentOrder.setCompanyId(company.getId());
        currentOrder.setCompanyName(company.getName());
        currentOrder.setWarehouseId(warehouse.getId());
        currentOrder.setWarehouseName(warehouse.getName());

        // Converte para DTO e envia ao backend
        orderService.createOrderAsync(OrderMapper.toDTO(currentOrder), token)
                .thenAccept(orderDTO -> Platform.runLater(() -> {
                    String prettyJson = null;
                    try {
                        prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(orderDTO);
                    } catch (Exception e) {
                        System.out.println("Erro ao formatar JSON da encomenda: " + e.getMessage());
                    }
                    System.out.println("Encomenda enviada com sucesso!\n" + (prettyJson != null ? prettyJson : "Dados da encomenda não disponíveis"));
                    AlertUtil.showInfo("Sucesso", "Encomenda enviada com sucesso!");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.out.println("Erro ao enviar encomenda: " + ex.getMessage());
                        AlertUtil.showError("Erro", "Erro ao enviar encomenda!\n" + ex.getMessage());
                    });
                    return null;
                });

        clearForm();
    }

    private void loadBackendData() {
        orderService.fetchCompaniesAsync(token).thenAccept(companies ->
                Platform.runLater(() -> companiesData.setAll(companies)));

        orderService.fetchWarehousesAsync(token).thenAccept(warehouses ->
                Platform.runLater(() -> warehousesData.setAll(warehouses)));

        orderService.fetchProductsAsync(token).thenAccept(products ->
                Platform.runLater(() -> productsData.setAll(products)));
    }

    private boolean  isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }



    private void clearForm() {
        nameField.clear();
        nuitField.clear();
        phoneField.clear();
        emailField.clear();
        paymentField.clear();
        addressField.clear();
        notesArea.clear();
        orderItems.clear();
        totalLabel.setText("Total: 0.00");
        companyComboBox.getSelectionModel().clearSelection();
        warehouseComboBox.getSelectionModel().clearSelection();
        productComboBox.getSelectionModel().clearSelection();
        quantityField.clear();
        currentOrder = new Order();
        currentOrder.setStatus("PENDING");
        currentOrder.setOrderItems(new ArrayList<>());
    }
}
