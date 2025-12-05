package org.manager.controller;

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
import org.manager.dto.CategoryDTO;
import org.manager.dto.CompanyDTO;
import org.manager.service.CategoryService;
import org.manager.service.CompanyService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

public class CategoryController {
    private final CategoryService categoryService;
    private final CompanyService companyService;

    @FXML
    private TableView<CategoryDTO> categoryTable;

    @FXML
    private TableColumn<CategoryDTO, Long> idColumn;

    @FXML
    private TableColumn<CategoryDTO, String> nameColumn;

    @FXML
    private TableColumn<CategoryDTO, String> companyColumn;

    private final ObservableList<CategoryDTO> categoryData = FXCollections.observableArrayList();
    private final ObservableList<CompanyDTO> companyData = FXCollections.observableArrayList();

    private CategoryDTO editingCategory;
    private final String token = SessionManager.getToken();

    public CategoryController() {
        this.categoryService = new CategoryService();
        this.companyService = new CompanyService();
    }

    @FXML
    private void initialize() {
        setupTable();
        loadCategories();

    }

    /**
     * Configura as colunas da tabela
     */
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        categoryTable.setItems(categoryData);
    }


    @FXML
    private void deleteCategory() {
        CategoryDTO selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Erro", "Selecione uma categoria para deletar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja realmente excluir a categoria \"" + selected.getName() + "\"?");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                categoryService.deleteCategory(selected.getId(), token)
                        .thenAccept(category -> Platform.runLater(() -> {
                            categoryData.remove(selected);
                            AlertUtil.showInfo("Sucesso", "Categoria deletada!");
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> AlertUtil.showError("Erro", "Erro ao deletar categoria."));
                            ex.printStackTrace();
                            return null;
                        });
            }
        });
    }


    // ===================== CREATE / UPDATE =====================
    private void openCategoryForm(boolean isEdit, CategoryDTO data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CategoryForm.fxml"));
            Parent root = loader.load();
            CategoryFormController controller = loader.getController();

            controller.setEditMode(isEdit);
            controller.setCategoryController(this);

            if (isEdit) {
                controller.setEditingCategory(data);
                controller.changeTitle("Atualizar Categoria");
            } else {
                controller.changeTitle("Cadastro de Categoria");
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
    private void handleOpenFormCreateCategory() {
        openCategoryForm(false, null);
    }

    @FXML
    private void handleOpenFormUpdateCategory() {
        CategoryDTO selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            AlertUtil.showError("Alerta", "Selecione primeiro a categoria.");
            return;
        }
        openCategoryForm(true, selectedCategory);
    }


    public void refreshCategoryTable() {
        loadCategories();
    }

    private void loadCategories() {
        categoryService.getAllCategories(token)
                .thenAccept(list -> Platform.runLater(() ->
                        categoryData.setAll(list)))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Erro ao carregar categorias."));
                    return null;
                });
    }
}
