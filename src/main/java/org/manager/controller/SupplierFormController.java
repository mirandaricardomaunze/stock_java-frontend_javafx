package org.manager.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.manager.dto.CompanyDTO;
import org.manager.dto.SupplierDTO;
import org.manager.service.CompanyService;
import org.manager.service.SupplierService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;
import org.manager.util.SetupComboBoxDisplay;

public class SupplierFormController {
    String token= SessionManager.getToken();

    @FXML private Label titleModal;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField nuitField;
    @FXML private TextField addressField;
    @FXML private TextField websiteField;
    @FXML private TextField notesField;
    @FXML private Button btnUpdateAndCreate;
    @FXML private ComboBox<CompanyDTO>  companyComboBox;
    private SupplierDTO editingSupplier;

    ObservableList<CompanyDTO>companyData= FXCollections.observableArrayList();
    ObservableList<SupplierDTO>supplierData= FXCollections.observableArrayList();

    private final SupplierService  supplierService;
    private final CompanyService companyService;
    public SupplierFormController(){
        this.supplierService=new SupplierService();
        this.companyService=new CompanyService();
    }

    private SupplierController supplierController;
    public void setSupplierController(SupplierController supplierController){
        this.supplierController=supplierController;
    }

    @FXML
    private void initialize(){
        loadCompanies();
        SetupComboBoxDisplay.setupComboBoxDisplay(companyComboBox,CompanyDTO::getName);
    }

    private void loadCompanies(){
        companyService.getAllCompanies()
                .thenAccept(company->{
                    Platform.runLater(()->{
                        companyData.setAll(company);
                        companyComboBox.setItems(companyData);
                    });
                })
                .exceptionally(ex->{Platform.runLater(()->{
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Erro ao carregar empresas."));
                });
                    return null;
                });
    }

    private void createSupplier(){
        SupplierDTO supplier=buildSupplierForm();
        if (!validationForm())return;
        supplierService.createSupplier(supplier,token)
                .thenAccept(supplierCreated-> {
                    Platform.runLater(()->{
                        supplierData.add(supplierCreated);
                        refreshSuplliersTable();
                        AlertUtil.showInfo("Sucesso", "Fornecedor cadastrado!");
                        closeForm();
                    });
                })
                .exceptionally(ex->{
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao atualizar fornecedor."));
                    return null;
                });
    }

    @FXML
    private void handleUpdateAndCreate() {
        String action=btnUpdateAndCreate.getText().toLowerCase();
        if (action.contains("atualizar")){
            updateSupplier();
        }else {
            createSupplier();
        }
    }
    private void updateSupplier(){
        if (editingSupplier == null) {
            AlertUtil.showError("Erro", "Selecione um fornecedor para editar.");
            return;
        }
        if (editingSupplier.getId() == null) {
            AlertUtil.showError("Erro", "Fornecedor sem ID — não é possível atualizar.");
            return;
        }

        SupplierDTO updateSupplier=buildSupplierForm();
        if (updateSupplier==null)return;
        updateSupplier.setId(editingSupplier.getId());
        supplierService.updateSupplier(updateSupplier,updateSupplier.getId(),token)
                .thenAccept(supplier->Platform.runLater(()->{
                    int index=supplierData.indexOf(editingSupplier);
                    if (index>=0)supplierData.set(index,supplier);
                    refreshSuplliersTable();
                    AlertUtil.showInfo("Sucesso", "Fornecedor atualizado!");
                    closeForm();
                }))
                .exceptionally(ex->{
                    Platform.runLater(()->{
                        Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao atualizar fornecedor."));
                    });
                    return null;
                });
    }

    private SupplierDTO buildSupplierForm(){
        try {
            CompanyDTO selectedCompany = companyComboBox.getValue();
            if (selectedCompany == null) {
                AlertUtil.showError("Erro", "Selecione uma empresa.");
                return null;
            }

            SupplierDTO supplier=new SupplierDTO();
            supplier.setName(nameField.getText().trim());
            supplier.setEmail(emailField.getText().trim());
            supplier.setPhone(phoneField.getText().trim());
            supplier.setWebsite(websiteField.getText().trim());
            supplier.setNuit(nuitField.getText().trim());
            supplier.setAddress(addressField.getText().trim());
            supplier.setNotes(notesField.getText().trim());
            supplier.setCompanyId(selectedCompany.getId());
            supplier.setCompany(selectedCompany.getName());
            return supplier;
        } catch (Exception e) {
            AlertUtil.showError("Erro", "Dados inválidos no formulário");
            throw new RuntimeException(e);
        }
    }
    public void editForm(SupplierDTO supplier) {
        editingSupplier = supplier;
        nameField.setText(supplier.getName());
        emailField.setText(supplier.getEmail());
        phoneField.setText(supplier.getPhone());
        nuitField.setText(supplier.getNuit());
        addressField.setText(supplier.getAddress());
        websiteField.setText(supplier.getWebsite());
        notesField.setText(supplier.getNotes());

        companyData.stream()
                .filter(c -> c.getId().equals(supplier.getCompanyId()))
                .findFirst()
                .ifPresent(companyComboBox::setValue);
    }
    private void refreshSuplliersTable(){
        supplierController.refreshTable();
    }

    public void changeTitle(String title){
        if (title!=null){
            titleModal.setText(title);
        }
    }

    public void setEditMode(boolean isEditMode){
        if (isEditMode){
            titleModal.setText("Atualizar Fornecedor");
            btnUpdateAndCreate.setText("Atualizar ");
        }else {
            titleModal.setText("Cadastrar Fornecedor");
            btnUpdateAndCreate.setText("Cadastrar");
        }
    }

    @FXML
    private void handleCancel(){
        closeForm();
    }

    private void closeForm(){
        Stage stage=(Stage) nameField.getScene().getWindow();
        if (stage!=null){
            stage.close();
        }
    }
    private boolean validationForm(){
        if (nameField.getText().isEmpty()||
                emailField.getText().isEmpty()||
                nuitField.getText().isEmpty()||
                websiteField.getText().isEmpty()||
                notesField.getText().isEmpty()||
                phoneField.getText().isEmpty()||
                addressField.getText().isEmpty()
        ){
          AlertUtil.showError("Alerta","Preenche todos campos!");
          return false;
        } else if (companyComboBox.getValue()==null) {
            AlertUtil.showError("Alerta","Seleciona a empresa!");
            return false;
        }
        return true;
    }
}
