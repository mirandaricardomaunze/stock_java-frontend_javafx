package org.manager.controller;

import javafx.application.Platform;
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
import org.manager.dto.ProductResponseDTO;
import org.manager.service.ProductService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

import java.io.IOException;
import java.math.BigDecimal;

public class ProductController {

    @FXML private TableView<ProductResponseDTO> productsTable;

    // ==== COLUNAS ====
    @FXML private TableColumn<ProductResponseDTO, Long> colId;
    @FXML private TableColumn<ProductResponseDTO, String> colName;
    @FXML private TableColumn<ProductResponseDTO, String> colCategory;
    @FXML private TableColumn<ProductResponseDTO, BigDecimal> colSellingPrice;
    @FXML private TableColumn<ProductResponseDTO, BigDecimal> colCostPrice;
    @FXML private TableColumn<ProductResponseDTO, Integer> colQuantity;
    @FXML private TableColumn<ProductResponseDTO, Integer> colMinStock;
    @FXML private TableColumn<ProductResponseDTO, String> colSupplier;
    @FXML private TableColumn<ProductResponseDTO, String> colUnit;
    @FXML private TableColumn<ProductResponseDTO, String> colBarcode;
    @FXML private TableColumn<ProductResponseDTO, String> colReference;
    @FXML private TableColumn<ProductResponseDTO, String> colCompany;
    @FXML private TableColumn<ProductResponseDTO, String> colWarehouse;

    // Pesquisa
    @FXML private TextField txtSearch;

    // Service
    private final ProductService productService = new ProductService();

    // Dados da tabela
    private final ObservableList<ProductResponseDTO> allProductsData = FXCollections.observableArrayList();
    private FilteredList<ProductResponseDTO> filteredData;
    private SortedList<ProductResponseDTO> sortedData;

    private final String token = SessionManager.getToken();

    // ========================= INIT =========================
    @FXML
    private void initialize() {
        setupTable();
        loadProducts();
        setupSearch();
    }

    // ========================= TABELA =========================
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName")); // troque por categoryName se existir
        colSellingPrice.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        colCostPrice.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantityInStock"));
        colMinStock.setCellValueFactory(new PropertyValueFactory<>("minimumStockLevel"));
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName")); // troque por supplierName se existir
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasure"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colReference.setCellValueFactory(new PropertyValueFactory<>("referenceNumber"));
        colCompany.setCellValueFactory(new PropertyValueFactory<>("companyName")); // troque por companyName se existir
        colWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName")); // troque por warehouseName se existir
    }

    // ========================= LOAD =========================
    private void loadProducts() {
        productService.getAllProducts(token)
                .thenAccept(products -> Platform.runLater(() -> allProductsData.setAll(products)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao carregar produtos."));
                    ex.printStackTrace();
                    return null;
                });
    }

    // ========================= SEARCH =========================
    private void setupSearch() {
        filteredData = new FilteredList<>(allProductsData, p -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productsTable.comparatorProperty());
        productsTable.setItems(sortedData);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterProducts(newVal));
    }

    @FXML
    private void handleSearchProduct() {
        String filter = txtSearch.getText();
        filterProducts(filter);
    }

    private void filterProducts(String filter) {
        if (filteredData == null) return;

        filteredData.setPredicate(product -> {
            if (filter == null || filter.isEmpty()) return true;
            String lowerFilter = filter.toLowerCase();

            return product.getName().toLowerCase().contains(lowerFilter)
                    || product.getSku().toLowerCase().contains(lowerFilter)
                    || product.getBarcode().toLowerCase().contains(lowerFilter)
                    || product.getReferenceNumber().toLowerCase().contains(lowerFilter)
                    || (product.getBrand() != null && product.getBrand().toLowerCase().contains(lowerFilter))
                    || (product.getModel() != null && product.getModel().toLowerCase().contains(lowerFilter))
                    || (product.getTags() != null && product.getTags().toLowerCase().contains(lowerFilter));
        });
    }

    // ========================= DELETE =========================
    @FXML
    private void handleDeleteProduct() {
        ProductResponseDTO selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Erro", "Selecione um produto.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
                "Confirmação",
                "Deseja remover o produto " + selected.getName() + "?"
        );
        if (!confirmed) return;

        productService.deleteProduct(selected.getId(), token)
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        allProductsData.remove(selected);
                        AlertUtil.showInfo("Sucesso", "Produto removido.");
                    } else {
                        AlertUtil.showError("Erro", "Falha ao remover produto.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao remover produto."));
                    ex.printStackTrace();
                    return null;
                });
    }

    // ========================= FORMULARIO =========================
    @FXML
    private void openProductForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductForm.fxml"));
            Parent root = loader.load();

            ProductFormController controller = loader.getController();
            controller.setProductController(this);
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
    private void updateOpenProductForm() {
        ProductResponseDTO selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Erro", "Selecione um produto.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductForm.fxml"));
            Parent root = loader.load();

            ProductFormController controller = loader.getController();
            controller.setProductController(this);
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

    public void refreshTable() {
        loadProducts();
    }
}
