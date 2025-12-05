package org.manager.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.manager.appContext.AppContext;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

public class MainController {

    @FXML
    private Label userLabel;

    @FXML
    private StackPane mainContainer;

    @FXML
    public void initialize() {
        AppContext.setMainController(this);
        String user = SessionManager.getCurrentUser();
        if (user != null && !user.isEmpty()) {
            userLabel.setText(" " + user.toUpperCase().charAt(0));
        } else {
            userLabel.setText("Bem-vindo, usuário");
        }
    }

    // 🔹 Método genérico para carregar qualquer FXML
    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContainer.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erro", "Falha ao carregar a tela: " + fxmlPath);
        }
    }

    // 🔹 Métodos públicos para FXML, chamam o método genérico
    public void loadDashboard() { loadView("/fxml/Dashboard.fxml"); }
    public void loadSupplier() { loadView("/fxml/Supplier.fxml"); }
    public void loadOrder() { loadView("/fxml/Order.fxml"); }
    public void loadInvoice() { loadView("/fxml/Invoice.fxml"); }
    public void loadCategory() { loadView("/fxml/Category.fxml"); }
    public void loadWarehouse() { loadView("/fxml/Warehouse.fxml"); }
    public void loadUserRegister() { loadView("/fxml/UserRegister.fxml"); }
    public void loadProductRegister() { loadView("/fxml/Product.fxml"); }
    public void loadCompanyRegister() { loadView("/fxml/Company.fxml"); }
    public void loadOrderTable(){loadView("/fxml/OrderTable.fxml");}
    public void loadMainLinks(){loadView("/fxml/MainLinks.fxml");}
    public void loadInvoicesTable(){loadView("/fxml/InvoiceTable.fxml");}
    public  void loadMainDashboard(){loadView("/fxml/MainDashboard.fxml");}
    public  void loadSales(){loadView("/fxml/Sale.fxml");}
    public  void loadTransfer(){loadView("/fxml/Transfer.fxml");}
    public  void loadMovements(){loadView("/fxml/Movements.fxml");}
    public  void loadStock(){loadView("/fxml/Stock.fxml");}

}
