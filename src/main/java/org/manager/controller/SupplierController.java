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
import org.manager.dto.CompanyDTO;
import org.manager.dto.SupplierDTO;
import org.manager.service.CompanyService;
import org.manager.service.SupplierService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;
import java.io.IOException;

public class SupplierController {

    private final SupplierService supplierService;
    private final CompanyService companyService;
    private final String token = SessionManager.getToken();


    @FXML
    private TableView<SupplierDTO> supplierTable;
    @FXML
    private TableColumn<SupplierDTO, Long> idColumn;
    @FXML
    private TableColumn<SupplierDTO, String> nameColumn;
    @FXML
    private TableColumn<SupplierDTO, String> emailColumn;
    @FXML
    private TableColumn<SupplierDTO, String> phoneColumn;
    @FXML
    private TableColumn<SupplierDTO, String> nuitColumn;
    @FXML
    private TableColumn<SupplierDTO, String> addressColumn;
    @FXML
    private TableColumn<SupplierDTO, String> websiteColumn;
    @FXML
    private TableColumn<SupplierDTO, String> notesColumn;
    @FXML
    private TableColumn<SupplierDTO, String> companyColumn;

    private FilteredList<SupplierDTO>filteredListData;
    private SortedList<SupplierDTO>sortedListData;

    @FXML private TextField txtSearch;
    private SupplierDTO editingSupplier;

    private final ObservableList<CompanyDTO> companyData = FXCollections.observableArrayList();
    private final ObservableList<SupplierDTO> supplierData = FXCollections.observableArrayList();

    public SupplierController() {
        this.supplierService = new SupplierService();
        this.companyService = new CompanyService();
    }

    @FXML
    private void initialize() {
        setTable();
        loadSuppliers();
        setUpSearchRealTimeSuppliers();
    }

    private void setUpSearchRealTimeSuppliers(){
        filteredListData=new FilteredList<>(supplierData,supplier->true);
        sortedListData=new SortedList<>(filteredListData);
        sortedListData.comparatorProperty().bind(supplierTable.comparatorProperty());
        supplierTable.setItems(sortedListData);
        txtSearch.textProperty().addListener((observable, oldValue, newValue)->{
           handleSearchRealTimeSuppliers(newValue);
        });
    }

    @FXML
    private void handleSearchButton() {
        String searchText = txtSearch.getText();
        handleSearchRealTimeSuppliers(searchText);
    }


    private void handleSearchRealTimeSuppliers(String searchText){
       if (searchText == null || searchText.isBlank()){
           filteredListData.setPredicate(supplier->true);
       }else{
           String lowerCaseFilter=searchText.toLowerCase().trim();
           filteredListData.setPredicate(supplier->{
               if(supplier.getNuit()!=null && supplier.getNuit().toLowerCase().contains(lowerCaseFilter))return true;
               if(supplier.getNotes()!=null && supplier.getNotes().toLowerCase().contains(lowerCaseFilter))return true;
               if(supplier.getName()!=null && supplier.getName().toLowerCase().contains(lowerCaseFilter))return true;
               if(supplier.getAddress()!=null && supplier.getAddress().toLowerCase().contains(lowerCaseFilter))return true;
               if(supplier.getEmail()!=null && supplier.getEmail().toLowerCase().contains(lowerCaseFilter))return true;
               if(supplier.getPhone()!=null && supplier.getPhone().toLowerCase().contains(lowerCaseFilter))return true;
               return false;
           });
       }
       supplierTable.refresh();
    }

    @FXML
    private void handleDeleteSupplier() {
        SupplierDTO selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Erro", "Selecione um fornecedor para eliminar.");
            return;
        }

        // aqui entra a confirmação
        boolean confirmed = AlertUtil.showConfirmation(
                "Confirmação",
                "Tem certeza que deseja eliminar o fornocedor'" + selected.getName() + "'?"
        );

        if (!confirmed) {
            return;
        }

        supplierService.deleteSupplier(selected.getId(), token)
                .thenAccept(deleted -> Platform.runLater(() -> {
                    supplierData.remove(selected);
                    AlertUtil.showInfo("Sucesso", "Fornecedor removido!");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao remover fornecedor."));
                    ex.printStackTrace();
                    return null;
                });
    }

    /** -------------------- Carregar Fornecedores -------------------- */
    private void loadSuppliers() {
        supplierService.getAllSuppliers(token)
                .thenAccept(suppliers -> Platform.runLater(() -> {
                    supplierData.setAll(suppliers);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Erro ao carregar fornecedores."));
                    ex.printStackTrace();
                    return null;
                });
    }


    /** -------------------- Configurar Tabela -------------------- */
    private void setTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        nuitColumn.setCellValueFactory(new PropertyValueFactory<>("nuit"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("website"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Mostrar nome da empresa em vez do objeto
        companyColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCompany()!= null ?
                                cellData.getValue().getCompany() : ""
                )
        );
    }


    public void refreshTable(){
        loadSuppliers();
    }

    @FXML
    private void openUpdateSupplierForm(){
        SupplierDTO selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();

        if (selectedSupplier != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SupplierForm.fxml"));
                Parent root = loader.load();
                SupplierFormController supplierFormController = loader.getController();
                supplierFormController.setEditMode(true);
                supplierFormController.changeTitle("Atualizar o fornecedor");
                supplierFormController.editForm(selectedSupplier);
                supplierFormController.setSupplierController(this);
                Stage stage = new Stage();
                stage.initStyle(StageStyle.UNDECORATED);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();
            } catch (IOException e) {
                AlertUtil.showError("Erro", "Erro ao carregar o formulário.");
                throw new RuntimeException(e);
            }
        }else {
            AlertUtil.showError("Alerta", "Seleciona primeiro o fornecedor.");
        }
    }
    @FXML
    private void openCreateSupplierForm(){
        try {
            FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/SupplierForm.fxml"));
            Parent root= loader.load();
            SupplierFormController supplierFormController=loader.getController();
            supplierFormController.setEditMode(false);
            supplierFormController.changeTitle("Cadastro do fornecedor");
            supplierFormController.setSupplierController(this);
            Stage stage=new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            AlertUtil.showError("Erro", "Erro ao carregar o formulário.");
            throw new RuntimeException(e);
        }
    }
}
