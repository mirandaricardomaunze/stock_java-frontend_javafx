package org.manager.controller;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.manager.appContext.AppContext;
import org.manager.service.CategoryService;
import org.manager.service.ProductService;
import org.manager.session.SessionManager;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardController {
    private final ProductService productService ;
    private final CategoryService categoryService;

    public  DashboardController() {

        this.productService = new ProductService();
        this.categoryService = new CategoryService();
    }

    private  final String token = SessionManager.getToken();
    private final Long companyId = SessionManager.getCurrentCompanyId();

    @FXML
    private void initialize() {
        loadDataTotalProducts();
        loadDataLowStockProducts();
        loadDataTotalValueProducts();
        loadDataTotalCategories();

    }

    @FXML
    private BorderPane root;

    @FXML
    private Label lowStockProductsLabel;

    @FXML
    private Label totalValueLabel;

    @FXML
    private Label totalCategoriesLabel;

    @FXML
    private Label totalProductsLabel;

    @FXML
    private ComboBox periodCombobox;

    @FXML
    private Label totalSalesLabel;

    @FXML
    private void goToDashboard() {
        try {
            AppContext.getMainController().loadDashboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToMainLinks() {
        try {
            AppContext.getMainController().loadMainLinks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSupplier() {
        try {
            AppContext.getMainController().loadSupplier();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void goToOrderTable(){
        try {
            AppContext.getMainController().loadOrderTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void goToCategory() {
        try {
            AppContext.getMainController().loadCategory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToWarehouse() {
        try {
            AppContext.getMainController().loadWarehouse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void goToOrder() {
        try {
            AppContext.getMainController().loadOrder();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void goToProductRegister() {
        try {
            AppContext.getMainController().loadProductRegister();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadDataTotalProducts() {
        productService.getTotalProductsInCompany(companyId, token)
                .thenAccept(total -> {
                    // Atualiza UI no thread do JavaFX
                    Platform.runLater(() -> {
                        totalProductsLabel.setText(String.valueOf(total));
                        System.out.println("Total de produtos: " + total);
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    System.out.println("Erro ao carregar total de produtos: " + ex.getMessage());
                    return null;
                });
    }

    public void loadDataLowStockProducts() {
        productService.getProductsBelowMinStock(companyId, token)
                .thenAccept(total -> {
                    // Atualiza UI no thread do JavaFX
                    Platform.runLater(() -> {
                        lowStockProductsLabel.setText(String.valueOf(total));
                        System.out.println("Total de produtos com baixo estoque: " + total);
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    System.out.println("Erro ao carregar total de produtos com quantidade menor que o mínimo em estoque: " + ex.getMessage());
                    return null;
                });
    }


    public void loadDataTotalValueProducts() {
        productService.getTotalValueOfProducts(companyId, token)
                .thenAccept(total -> {
                    // Atualiza UI no thread do JavaFX
                    Platform.runLater(() -> {
                        totalValueLabel.setText(String.valueOf(total));
                        System.out.println("Valor total de produtos: " + total);
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    System.out.println("Erro ao carregar valor total de produtos: " + ex.getMessage());
                    return null;
                });
    }

    public void loadDataTotalCategories() {
        try {
            categoryService.getTotalOfCategoriesByCompanyId(companyId, token)
                    .thenAccept(total -> {
                        // Atualiza UI no thread do JavaFX
                        Platform.runLater(() -> {
                            totalCategoriesLabel.setText(String.valueOf(total));
                            System.out.println("Total de categorias: " + total);
                        });
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        System.out.println("Erro ao carregar total de categorias: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao carregar total de categorias: " + e.getMessage());
        }
    }






}
