package org.manager.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.manager.appContext.AppContext;
import org.manager.dto.InvoiceDTO;
import org.manager.service.InvoiceService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Controlador responsável pela tabela de Faturas (InvoiceTable.fxml)
 */
public class InvoiceTableController {

    // --- Componentes FXML ---
    @FXML private TextField searchInvoiceField;

    @FXML private TableView<InvoiceDTO> invoiceTable;
    @FXML private TableColumn<InvoiceDTO, String> colInvOrderNumber;
    @FXML private TableColumn<InvoiceDTO, String> colInvCustomer;
    @FXML private TableColumn<InvoiceDTO, String> colInvDate;
    @FXML private TableColumn<InvoiceDTO, Double> colInvTotal;

    @FXML private Button btnPDF;
    @FXML private Button btnExcel;
    @FXML private Button btnUpdat;

    // --- Dados ---
    private final ObservableList<InvoiceDTO> invoices = FXCollections.observableArrayList();
    private FilteredList<InvoiceDTO> filteredInvoices;

    private final InvoiceService invoiceService = new InvoiceService();
    private final String token = SessionManager.getToken();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());

    // --- Inicialização ---
    @FXML
    public void initialize() {
        setupInvoiceTable();
        setupSearch();
        loadInvoices();
    }

    /** Configura as colunas da tabela */
    private void setupInvoiceTable() {
        colInvOrderNumber.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getInvoiceNumber()));
        colInvCustomer.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCustomerName()));
        colInvDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getInvoiceDate() != null
                        ? DATE_FORMATTER.format(data.getValue().getInvoiceDate())
                        : ""));
        colInvTotal.setCellValueFactory(data ->
                new SimpleDoubleProperty(
                        data.getValue().getTotalAmount() != null
                                ? data.getValue().getTotalAmount().doubleValue()
                                : 0.0).asObject());

        // usa FilteredList para pesquisa eficiente
        filteredInvoices = new FilteredList<>(invoices, p -> true);
        invoiceTable.setItems(filteredInvoices);
    }

    /** Configura pesquisa em tempo real */
    private void setupSearch() {
        if (searchInvoiceField == null) return;

        searchInvoiceField.textProperty().addListener((obs, oldText, newText) -> {
            final String query = (newText == null) ? "" : newText.trim().toLowerCase();
            if (query.isEmpty()) {
                filteredInvoices.setPredicate(p -> true);
            } else {
                filteredInvoices.setPredicate(createInvoicePredicate(query));
            }
        });
    }

    /** Define o comportamento de filtragem */
    private Predicate<InvoiceDTO> createInvoicePredicate(String lowerQuery) {
        return invoice -> {
            if (invoice == null) return false;
            if (invoice.getInvoiceNumber() != null &&
                    invoice.getInvoiceNumber().toLowerCase().contains(lowerQuery)) return true;
            if (invoice.getCustomerName() != null &&
                    invoice.getCustomerName().toLowerCase().contains(lowerQuery)) return true;
            if (invoice.getInvoiceDate() != null &&
                    DATE_FORMATTER.format(invoice.getInvoiceDate()).toLowerCase().contains(lowerQuery))
                return true;
            return false;
        };
    }

    /** Carrega a lista de faturas */
    private void loadInvoices() {
        invoiceService.fetchInvoicesAsync(token)
                .thenAccept(list -> Platform.runLater(() -> {
                    invoices.setAll(list);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Falha ao carregar faturas: " + ex.getMessage()));
                    return null;
                });
    }

    // ===============================================================
    // === Funções de Exportação / Impressão / Navegação ============
    // ===============================================================

    @FXML
    private void exportInvoiceFilePdf() { exportInvoiceFile("pdf"); }

    @FXML
    private void exportInvoiceFileExcel() { exportInvoiceFile("excel"); }

    @FXML
    private void printInvoice() {
        InvoiceDTO selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("Atenção", "Selecione uma fatura para imprimir.");
            return;
        }

        invoiceService.exportPdfAsync(selected.getId(), token)
                .thenAccept(bytes -> Platform.runLater(() -> {
                    try {
                        File tempFile = File.createTempFile("fatura_" + selected.getInvoiceNumber(), ".pdf");
                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                            fos.write(bytes);
                        }

                        var desktop = java.awt.Desktop.getDesktop();
                        if (desktop.isSupported(java.awt.Desktop.Action.PRINT)) {
                            desktop.print(tempFile);
                            AlertUtil.showInfo("Impressão", "Fatura enviada para a impressora.");
                        } else {
                            AlertUtil.showError("Erro", "Impressão direta não suportada neste sistema.");
                        }
                    } catch (Exception e) {
                        AlertUtil.showError("Erro", "Falha ao imprimir: " + e.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Falha ao gerar PDF: " + ex.getMessage()));
                    return null;
                });
    }

    /** Exporta PDF ou Excel */
    private void exportInvoiceFile(String type) {
        InvoiceDTO selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("Atenção", "Selecione uma fatura para exportar.");
            return;
        }

        var future = type.equalsIgnoreCase("pdf")
                ? invoiceService.exportPdfAsync(selected.getId(), token)
                : invoiceService.exportExcelAsync(selected.getId(), token);

        future.thenAccept(bytes -> saveFile(bytes, "invoice_" + selected.getInvoiceNumber() + "." + type))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Falha ao exportar " + type.toUpperCase() + ": " + ex.getMessage()));
                    return null;
                });
    }

    /** Salva o arquivo gerado localmente */
    private void saveFile(byte[] bytes, String fileName) {
        new Thread(() -> {
            try {
                File file = new File(System.getProperty("user.home") + "/Downloads/" + fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(bytes);
                }
                Platform.runLater(() ->
                        AlertUtil.showInfo("Sucesso", "Arquivo salvo em: " + file.getAbsolutePath()));
            } catch (Exception e) {
                Platform.runLater(() ->
                        AlertUtil.showError("Erro", "Falha ao salvar arquivo: " + e.getMessage()));
            }
        }).start();
    }

    /** Botão para limpar campo de pesquisa */
    @FXML
    private void clearSearch() {
        if (searchInvoiceField != null) searchInvoiceField.clear();
    }

    /** Navega de volta para o menu de faturas principal */
    @FXML
    private void goToInvoices() {
        try {
            AppContext.getMainController().loadInvoice();
        } catch (Exception e) {
            AlertUtil.showError("Erro", "Falha ao abrir tela de faturas: " + e.getMessage());
        }
    }
}
