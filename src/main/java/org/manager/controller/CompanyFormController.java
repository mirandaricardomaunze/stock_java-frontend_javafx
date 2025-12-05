package org.manager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.manager.dto.CompanyDTO;
import org.manager.service.CompanyService;
import org.manager.util.AlertUtil;

public class CompanyFormController {

    // 🏷️ Campos do formulário
    @FXML private Label titleModal;
    @FXML private TextField nameField, addressField, emailField, phoneField, nuitField, websiteField;
    @FXML private TextArea descriptionField;
    @FXML private TextField registrationField, logoField, countryField, cityField, postalCodeField, industryField;
    @FXML private TextField companyContactEmailField, companyContactPhoneField;
    @FXML private Button btnUpdateAndCreate;

    // 🧩 Dependências e dados
    private final CompanyService companyService = new CompanyService();
    private ObservableList<CompanyDTO> companyData = FXCollections.observableArrayList();
    private CompanyController companyController;
    private CompanyDTO editingCompany;

    // ⚙️ Inicialização
    @FXML
    private void initialize() {
        loadCompanies();
    }

    public void setCompanyController(CompanyController companyController) {
        this.companyController = companyController;
    }

    // 🔄 Carregar empresas (para sincronização interna)
    private void loadCompanies() {
        companyService.getAllCompanies()
                .thenAccept(companies -> Platform.runLater(() -> {
                    companyData.setAll(companies);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        String msg = "Erro ao carregar empresas: " + ex.getMessage();
                        System.err.println(msg);
                        AlertUtil.showError("Erro", msg);
                    });
                    return null;
                });
    }

    // 🧱 Construir objeto a partir do formulário
    private CompanyDTO buildCompanyFromForm() {
        CompanyDTO dto = new CompanyDTO();
        dto.setName(nameField.getText());
        dto.setAddress(addressField.getText());
        dto.setEmail(emailField.getText());
        dto.setPhoneNumber(phoneField.getText());
        dto.setTaxId(nuitField.getText());
        dto.setWebsite(websiteField.getText());
        dto.setDescription(descriptionField.getText());
        dto.setRegistrationNumber(registrationField.getText());
        dto.setLogoUrl(logoField.getText());
        dto.setCountry(countryField.getText());
        dto.setCity(cityField.getText());
        dto.setPostalCode(postalCodeField.getText());
        dto.setIndustry(industryField.getText());
        dto.setContactEmail(companyContactEmailField.getText());
        dto.setContactPhone(companyContactPhoneField.getText());
        return dto;
    }

    // 🆕 Criar empresa
    private void createCompany() {
        if (!validateForm()) return;

        try {
            CompanyDTO companyDTO = buildCompanyFromForm();
            companyService.createCompany(companyDTO)
                    .thenAccept(createdCompany -> Platform.runLater(() -> {
                        companyData.add(createdCompany);
                        AlertUtil.showInfo("Sucesso", "Empresa criada com sucesso!");
                        clearFormFields();
                    }))
                    .exceptionally(ex -> handleAsyncError(ex, "Erro ao criar empresa"));
        } catch (JsonProcessingException e) {
            AlertUtil.showError("Erro", "Falha ao processar dados: " + e.getMessage());
        }
    }

    // ✏️ Atualizar empresa existente
    private void updateCompany() {
        if (editingCompany == null) {
            AlertUtil.showError("Erro", "Nenhuma empresa selecionada para atualização.");
            return;
        }
        if (!validateForm()) return;

        try {
            CompanyDTO updated = buildCompanyFromForm();
            updated.setId(editingCompany.getId());

            companyService.updateCompany(updated)
                    .thenAccept(updatedCompany -> Platform.runLater(() -> {
                        int index = companyData.indexOf(editingCompany);
                        if (index >= 0) companyData.set(index, updatedCompany);
                        AlertUtil.showInfo("Sucesso", "Empresa atualizada com sucesso!");
                        closeForm();
                    }))
                    .exceptionally(ex -> handleAsyncError(ex, "Erro ao atualizar empresa"));
        } catch (JsonProcessingException e) {
            AlertUtil.showError("Erro", "Falha ao processar dados: " + e.getMessage());
        }
    }

    // 🧭 Lógica unificada do botão principal
    @FXML
    private void handleUpdateAndCreate() {
        String action = btnUpdateAndCreate.getText().toLowerCase();
        if (action.contains("atualizar")) updateCompany();
        else createCompany();
    }

    // 🧩 Preenche o formulário ao editar
    public void editFillForm(CompanyDTO company) {
        editingCompany = company;
        nameField.setText(company.getName());
        addressField.setText(company.getAddress());
        emailField.setText(company.getEmail());
        phoneField.setText(company.getPhoneNumber());
        nuitField.setText(company.getTaxId());
        websiteField.setText(company.getWebsite());
        descriptionField.setText(company.getDescription());
        registrationField.setText(company.getRegistrationNumber());
        logoField.setText(company.getLogoUrl());
        countryField.setText(company.getCountry());
        cityField.setText(company.getCity());
        postalCodeField.setText(company.getPostalCode());
        industryField.setText(company.getIndustry());
        companyContactEmailField.setText(company.getContactEmail());
        companyContactPhoneField.setText(company.getContactPhone());
    }

    // 🧾 Alterar título/modal
    public void changeTitle(String title) {
        if (title != null) titleModal.setText(title);
    }

    // 🔁 Modo criar/editar
    public void setEditMode(boolean isEditMode) {
        titleModal.setText(isEditMode ? "Atualizar Empresa" : "Cadastrar Empresa");
        btnUpdateAndCreate.setText(isEditMode ? "Atualizar" : "Cadastrar");
    }

    // ❌ Cancelar / Fechar formulário
    @FXML
    private void handleCancel() {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        if (stage != null) stage.close();
    }

    // ✅ Validação de campos obrigatórios
    private boolean validateForm() {
        TextField[] requiredFields = {
                nameField, addressField, emailField, phoneField,
                nuitField, websiteField, postalCodeField, countryField,
                cityField, industryField, logoField,
                companyContactEmailField, companyContactPhoneField
        };

        for (TextField field : requiredFields) {
            if (field.getText().trim().isEmpty()) {
                AlertUtil.showError("Erro", "Preencha todos os campos obrigatórios.");
                return false;
            }
        }
        if (descriptionField.getText().trim().isEmpty()) {
            AlertUtil.showError("Erro", "A descrição não pode estar vazia.");
            return false;
        }
        return true;
    }

    // ⚠️ Tratamento genérico para exceções assíncronas
    private Void handleAsyncError(Throwable ex, String context) {
        Platform.runLater(() -> {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            String msg = cause.getMessage() != null ? cause.getMessage() : "Erro desconhecido.";
            AlertUtil.showError("Erro", context + ": " + msg);
        });
        return null;
    }

    // 🧹 Limpa todos os campos do formulário
    private void clearFormFields() {
        nameField.clear();
        addressField.clear();
        emailField.clear();
        phoneField.clear();
        nuitField.clear();
        websiteField.clear();
        descriptionField.clear();
        registrationField.clear();
        logoField.clear();
        countryField.clear();
        cityField.clear();
        postalCodeField.clear();
        industryField.clear();
        companyContactEmailField.clear();
        companyContactPhoneField.clear();
        editingCompany = null; // reseta o objeto de edição
    }

}
