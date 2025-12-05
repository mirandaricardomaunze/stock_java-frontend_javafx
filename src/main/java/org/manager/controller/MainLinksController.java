package org.manager.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.manager.appContext.AppContext;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainLinksController implements Initializable {

    @FXML private Label dateDisplay;
    @FXML private Label timeDisplay;

    private Timeline timeline;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter timeFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDateTimeFormatters();
        startDateTimeUpdater();
    }

    private void setupDateTimeFormatters() {
        // Formato para data: "15 de Janeiro de 2024"
        dateFormatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));

        // Formato para hora: "09:30"
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    }

    private void startDateTimeUpdater() {
        // Atualizar imediatamente
        updateDateTime();

        // Criar timeline para atualizar a cada minuto
        timeline = new Timeline(new KeyFrame(
                Duration.minutes(1),
                e -> updateDateTime()
        ));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();

        // Formatar data
        String date = now.format(dateFormatter);

        // Formatar dia da semana
        String dayOfWeek = now.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        String capitalizedDayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1);

        // Formatar hora
        String time = now.format(timeFormatter);

        // Atualizar labels
        if (dateDisplay != null) {
            dateDisplay.setText(date);
        }
        if (timeDisplay != null) {
            timeDisplay.setText(capitalizedDayOfWeek + " - " + time);
        }
    }

    /* ===========================
       Botões do menu lateral
       =========================== */

    @FXML
    private void loadDashboard() {
        try {
            AppContext.getMainController().loadDashboard();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void loadMainLinks() {
        try {
            AppContext.getMainController().loadMainLinks();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void loadProductRegister() {
        try {
            AppContext.getMainController().loadProductRegister();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void loadInvoice() {
        try {
            AppContext.getMainController().loadInvoicesTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void loadCompanyRegister() {
        try {
            AppContext.getMainController().loadCompanyRegister();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void loadWarehouse() {
        try {
            AppContext.getMainController().loadWarehouse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void loadCategory() {
        try {
            AppContext.getMainController().loadCategory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void loadSupplier() {
        try {
            AppContext.getMainController().loadSupplier();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* ===========================
       Botões de Ações Rápidas
       =========================== */

    @FXML
    private void goToDashboard() {
        try {
            AppContext.getMainController().loadDashboard();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void goToOrderTable() {
        try {
            AppContext.getMainController().loadOrderTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void goToInvoiceTable() {
        try {
            AppContext.getMainController().loadInvoicesTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void goToClients() {
        try {
            // Se você tiver um método para carregar clientes, adicione aqui
            // AppContext.getMainController().loadClients();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void goToSale() {
        try {
            AppContext.getMainController().loadSales();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    private void goToMainDashbord() {
        try {
            AppContext.getMainController().loadMainDashboard();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void goToTransfer() {
        try {
            AppContext.getMainController().loadTransfer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    private void goToStock() {
        try {
            AppContext.getMainController().loadStock();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // Método para parar a timeline quando necessário
    public void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
        }
    }


}