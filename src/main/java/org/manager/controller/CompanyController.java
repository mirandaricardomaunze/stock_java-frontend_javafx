package org.manager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.manager.dto.CompanyDTO;
import org.manager.service.CompanyService;
import org.manager.util.AlertUtil;

public class CompanyController {

    @FXML private TextField searchField;

    // Tabela
    @FXML private TableView<CompanyDTO> companiesTable;
    @FXML private TableColumn<CompanyDTO, Long> idColumn;
    @FXML private TableColumn<CompanyDTO, String> nameColumn;
    @FXML private TableColumn<CompanyDTO, String> addressColumn;
    @FXML private TableColumn<CompanyDTO, String> emailColumn;
    @FXML private TableColumn<CompanyDTO, String> phoneColumn;
    @FXML private TableColumn<CompanyDTO, String> nuitColumn;
    @FXML private TableColumn<CompanyDTO, String> websiteColumn;
    @FXML private TableColumn<CompanyDTO, String> descriptionColumn;

    private final CompanyService companyService;
    private final ObservableList<CompanyDTO> companyData;

    private CompanyDTO selectedCompany;

    public CompanyController() {
        this.companyService = new CompanyService();
        this.companyData = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        setupTableColumns();
        loadCompanies();

        // Listener para pesquisa em tempo real
        searchField.textProperty().addListener((obs, oldValue, newValue) -> searchCompanies(newValue));
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        nuitColumn.setCellValueFactory(new PropertyValueFactory<>("taxId"));
        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("website"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        companiesTable.setItems(companyData);
    }

    public void refreshCompaniesTable() {
        loadCompanies();
    }

    private void loadCompanies() {
        companyService.getAllCompanies()
                .thenAccept(companies -> Platform.runLater(() -> {
                    companyData.clear();
                    companyData.addAll(companies);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.out.println("Erro ao carregar empresas: " + ex.getMessage());
                        AlertUtil.showError("Erro", "Erro ao carregar empresas: " + ex.getMessage());
                    });
                    return null;
                });
    }

    // ===================== PESQUISA =====================
    private void searchCompanies(String term) {
        if (term == null || term.isEmpty()) {
            loadCompanies();
            return;
        }

        String lowerTerm = term.toLowerCase();
        companyService.getAllCompanies()
                .thenAccept(companies -> Platform.runLater(() -> {
                    companyData.clear();
                    companyData.addAll(companies.stream()
                            .filter(c -> (c.getName() != null && c.getName().toLowerCase().contains(lowerTerm))
                                    || (c.getEmail() != null && c.getEmail().toLowerCase().contains(lowerTerm))
                                    || (c.getPhoneNumber() != null && c.getPhoneNumber().toLowerCase().contains(lowerTerm))
                                    || (c.getAddress() != null && c.getAddress().toLowerCase().contains(lowerTerm))
                            )
                            .toList());
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Erro na pesquisa: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void handleSearchCompanies() {
        searchCompanies(searchField.getText().trim());
        clearSearchField();
    }
     private void clearSearchField(){
        searchField.clear();
     }
    // ===================== CREATE / UPDATE =====================
    private void openCompanyForm(boolean isEdit, CompanyDTO data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CompanyForm.fxml"));
            Parent root = loader.load();
            CompanyFormController controller = loader.getController();

            controller.setEditMode(isEdit);
            controller.setCompanyController(this);

            if (isEdit) {
                controller.editFillForm(data);
                controller.changeTitle("Atualizar Empresa");
            } else {
                controller.changeTitle("Cadastro de Empresa");
            }

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenFormCreateCompany() {
        openCompanyForm(false, null);
    }

    @FXML
    private void handleOpenFormUpdateCompany() {
        CompanyDTO selectedCompany = companiesTable.getSelectionModel().getSelectedItem();
        if (selectedCompany == null) {
            AlertUtil.showError("Alerta", "Selecione primeiro a Empresa.");
            return;
        }
        openCompanyForm(true, selectedCompany);
    }

    @FXML
    private void handleDeleteCompany() {
        selectedCompany = companiesTable.getSelectionModel().getSelectedItem();
        if (selectedCompany == null) {
            AlertUtil.showError("Erro", "Selecione uma empresa para excluir.");
            return;
        }
        companyService.deleteCompany(selectedCompany.getId())
                .thenRun(() -> Platform.runLater(() -> {
                    companyData.remove(selectedCompany);
                    AlertUtil.showInfo("Sucesso", "Empresa excluída com sucesso.");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Erro ao excluir empresa: " + ex.getMessage()));
                    return null;
                });
    }
}
