package org.manager.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.manager.dto.CategoryDTO;
import org.manager.dto.CompanyDTO;
import org.manager.service.CategoryService;
import org.manager.service.CompanyService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;
import org.manager.util.SetupComboBoxDisplay;

public class CategoryFormController {

    @FXML private TextField nameField;
    @FXML private ComboBox<CompanyDTO> companyComboBox;
    @FXML private Button btnUpdateAndCreate;
    @FXML private Label titleModal;

    private final String token = SessionManager.getToken();

    private final CompanyService companyService;
    private final CategoryService categoryService;

    private CategoryDTO editingCategory;
    private CategoryController categoryController;

    private final ObservableList<CompanyDTO> companyData = FXCollections.observableArrayList();
    private final ObservableList<CategoryDTO> categoryData = FXCollections.observableArrayList();

    public CategoryFormController() {
        this.categoryService = new CategoryService();
        this.companyService = new CompanyService();
    }

    public void setCategoryController(CategoryController categoryController) {
        this.categoryController = categoryController;
    }

    @FXML
    public void initialize() {
        loadCompanies();
        SetupComboBoxDisplay.setupComboBoxDisplay(companyComboBox, CompanyDTO::getName);
    }

    /* =======================================================
       🟩 CRIAÇÃO DE CATEGORIA
    ======================================================= */
    private void createCategory() {
        if (!validateForm()) return;

        CategoryDTO category = buildCategoryForm();
        if (category == null) return;

        categoryService.createCategory(category, token)
                .thenAccept(created -> Platform.runLater(() -> {
                    categoryData.add(created);
                    refreshDataCategoriesTable();
                    AlertUtil.showInfo("Sucesso", "Categoria criada com sucesso!");
                    closeForm();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Erro ao criar categoria: " + ex.getMessage())
                    );
                    ex.printStackTrace();
                    return null;
                });
    }

    /* =======================================================
       🟨 ATUALIZAÇÃO DE CATEGORIA
    ======================================================= */
    private void updateCategory() {
        if (editingCategory == null) {
            AlertUtil.showError("Erro", "Nenhuma categoria selecionada para atualizar.");
            return;
        }

        CategoryDTO updatedCategory = buildCategoryForm();
        if (updatedCategory == null) return;

        updatedCategory.setId(editingCategory.getId());

        categoryService.updateCategory(updatedCategory.getId(), updatedCategory, token)
                .thenAccept(updated -> Platform.runLater(() -> {
                    int index = categoryData.indexOf(editingCategory);
                    if (index >= 0) categoryData.set(index, updated);
                    refreshDataCategoriesTable();
                    AlertUtil.showInfo("Sucesso", "Categoria atualizada com sucesso!");
                    closeForm();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Não foi possível atualizar a categoria: " + ex.getMessage())
                    );
                    ex.printStackTrace();
                    return null;
                });
    }

    /* =======================================================
       🧩 CONTROLE DE AÇÃO (CRIAR/ATUALIZAR)
    ======================================================= */
    @FXML
    private void handleUpdateAndCreate() {
        String action = btnUpdateAndCreate.getText().toLowerCase();
        if (action.contains("atualizar")) {
            updateCategory();
        } else {
            createCategory();
        }
    }

    /* =======================================================
       🏢 CARREGAMENTO DE EMPRESAS
    ======================================================= */
    private void loadCompanies() {
        companyService.getAllCompanies()
                .thenAccept(companies -> Platform.runLater(() -> {
                    companyData.setAll(companies);
                    companyComboBox.setItems(companyData);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            System.err.println("Erro ao carregar empresas: " + ex.getMessage())
                    );
                    return null;
                });
    }

    /* =======================================================
       🧱 CONSTRUÇÃO DO OBJETO
    ======================================================= */
    private CategoryDTO buildCategoryForm() {
        try {
            CompanyDTO selectedCompany = companyComboBox.getValue();
            if (selectedCompany == null) {
                AlertUtil.showError("Erro", "Selecione uma empresa.");
                return null;
            }

            CategoryDTO category = new CategoryDTO();
            category.setName(nameField.getText().trim());
            category.setCompanyId(selectedCompany.getId());
            category.setCompanyName(selectedCompany.getName());
            return category;

        } catch (Exception e) {
            AlertUtil.showError("Erro", "Dados inválidos no formulário.");
            return null;
        }
    }

    /* =======================================================
       ⚙️ MODO EDIÇÃO / TÍTULO
    ======================================================= */
    public void setEditingCategory(CategoryDTO category) {
        this.editingCategory = category;
        nameField.setText(category.getName());
        companyData.stream()
                .filter(c -> c.getId().equals(category.getCompanyId()))
                .findFirst()
                .ifPresent(c -> companyComboBox.getSelectionModel().select(c));
    }

    public void changeTitle(String title) {
        if (title != null) {
            titleModal.setText(title);
        }
    }

    public void setEditMode(boolean isEditMode) {
        if (isEditMode) {
            titleModal.setText("Atualizar Categoria");
            btnUpdateAndCreate.setText("Atualizar");
        } else {
            titleModal.setText("Cadastrar Categoria");
            btnUpdateAndCreate.setText("Cadastrar");
        }
    }

    /* =======================================================
       🧭 UTILITÁRIOS
    ======================================================= */
    private void refreshDataCategoriesTable() {
        if (categoryController != null) {
            categoryController.refreshCategoryTable();
        }
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        if (stage != null) stage.close();
    }

    private boolean validateForm() {
        if (nameField.getText().isEmpty()) {
            AlertUtil.showError("Erro", "Preencha o campo Nome.");
            return false;
        }
        if (companyComboBox.getValue() == null) {
            AlertUtil.showError("Erro", "Selecione uma empresa.");
            return false;
        }
        return true;
    }
}
