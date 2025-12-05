package org.manager.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.manager.appContext.AppContext;
import org.manager.service.CategoryService;
import org.manager.service.ProductService;
import org.manager.service.SaleService;
import org.manager.session.SessionManager;

import java.text.NumberFormat;
import java.util.Locale;

public class MainDashboardController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SaleService saleService;

    public MainDashboardController() {
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
        this.saleService = new SaleService();
    }

    private final String token = SessionManager.getToken();
    private final Long companyId = SessionManager.getCurrentCompanyId();

    @FXML private BorderPane root;
    @FXML private PieChart categoriasChart;
    @FXML private LineChart<String, Number> movimentacaoChart;
    @FXML private Label salesLabel;
    @FXML private Label profitLabel;
    @FXML private Label lowStockProductsLabel;
    @FXML private Label totalValueLabel;
    @FXML private Label totalCategoriesLabel;
    @FXML private Label totalProductsLabel;
    @FXML private ComboBox<String> filterComboBox;

    @FXML
    private void initialize() {

        filterComboBox.getSelectionModel().select("Último Mês");
        // Carregar métricas financeiras e gráficos com período inicial
        String defaultPeriod = mapPeriod(filterComboBox.getValue());

        loadMovementChart(defaultPeriod);
        // Carregar métricas principais
        loadDataTotalProducts();
        loadDataLowStockProducts();
        loadDataTotalValueProducts();
        loadDataTotalCategories();


        loadTotalSales(defaultPeriod);
        loadTotalProfit(defaultPeriod);
        loadCategoriasChart();

        // Listener do ComboBox: atualiza tudo ao mudar período
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            String period = mapPeriod(newVal);
            loadTotalSales(period);
            loadTotalProfit(period);
        });

        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            String period = mapPeriod(newVal);
            loadTotalSales(period);
            loadTotalProfit(period);
            loadMovementChart(period);
        });
    }

    // ======================== MÉTODOS DE NAVEGAÇÃO ========================
    @FXML private void goToDashboard() { AppContext.getMainController().loadDashboard(); }
    @FXML private void goToMainLinks() { AppContext.getMainController().loadMainLinks(); }
    @FXML private void goToSupplier() { AppContext.getMainController().loadSupplier(); }
    @FXML private void goToOrderTable() { AppContext.getMainController().loadOrderTable(); }
    @FXML private void goToCategory() { AppContext.getMainController().loadCategory(); }
    @FXML private void goToWarehouse() { AppContext.getMainController().loadWarehouse(); }
    @FXML private void goToOrder() { AppContext.getMainController().loadOrder(); }
    @FXML private void goToProductRegister() { AppContext.getMainController().loadProductRegister(); }

    // ======================== MÉTODOS DE CARREGAMENTO ========================
    private String mapPeriod(Object selected) {
        String period = (selected == null) ? "" : selected.toString().trim();
        return switch (period) {
            case "Últimos 3 Meses" -> "3months";
            case "Últimos 6 Meses" -> "6months";
            case "Último Ano" -> "1year";
            default -> "month";
        };
    }

    private void loadTotalSales(String period) {
        saleService.getTotalSales(period, token)
                .thenAccept(total -> Platform.runLater(() -> {
                    NumberFormat formatter = NumberFormat.getInstance(new Locale("pt", "MZ"));
                    formatter.setMinimumFractionDigits(2);
                    formatter.setMaximumFractionDigits(2);
                    salesLabel.setText("Mzn " + formatter.format(total));
                }))
                .exceptionally(ex -> { Platform.runLater(() -> salesLabel.setText("Mzn 0.00")); return null; });
    }

    private void loadTotalProfit(String period) {
        saleService.getProfit(period, token)
                .thenAccept(profit -> Platform.runLater(() -> {
                    NumberFormat formatter = NumberFormat.getInstance(new Locale("pt", "MZ"));
                    formatter.setMinimumFractionDigits(2);
                    formatter.setMaximumFractionDigits(2);
                    profitLabel.setText("Mzn " + formatter.format(profit));
                }))
                .exceptionally(ex -> { Platform.runLater(() -> profitLabel.setText("Mzn 0.00")); return null; });
    }

    private void loadMovementChart(String period) {
        if (period == null || period.isEmpty()) {
            period = "month";
        }
        saleService.getMonthlyMovement(companyId, period, token)
                .thenAccept(list -> Platform.runLater(() -> {
                    movimentacaoChart.getData().clear();

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Movimentação");
                    // Adiciona dados ao gráfico
                    list.forEach(dto -> series.getData().add(
                            new XYChart.Data<>(capitalize(dto.getMonth()), dto.getQuantity())
                    ));

                    movimentacaoChart.getData().add(series);
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }


    private void loadCategoriasChart() {
        productService.getProductsByCategory(companyId,token)
                .thenAccept(map -> Platform.runLater(() -> {
                    categoriasChart.getData().clear();
                    map.forEach((categoria, quantidade) -> {
                        PieChart.Data slice = new PieChart.Data(categoria, quantidade);
                        categoriasChart.getData().add(slice);
                    });
                }));
    }

    private void loadDataTotalProducts() {
        productService.getTotalProductsInCompany(companyId, token)
                .thenAccept(total -> Platform.runLater(() -> totalProductsLabel.setText(String.valueOf(total))))
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private void loadDataLowStockProducts() {
        productService.getProductsBelowMinStock(companyId, token)
                .thenAccept(total -> Platform.runLater(() -> lowStockProductsLabel.setText(String.valueOf(total))))
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private void loadDataTotalValueProducts() {
        productService.getTotalValueOfProducts(companyId, token)
                .thenAccept(total -> Platform.runLater(() -> totalValueLabel.setText(String.valueOf(total))))
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private void loadDataTotalCategories() {
        categoryService.getTotalOfCategoriesByCompanyId(companyId, token)
                .thenAccept(total -> Platform.runLater(() -> totalCategoriesLabel.setText(String.valueOf(total))))
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

}
