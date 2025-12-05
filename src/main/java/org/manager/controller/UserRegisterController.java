package org.manager.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.manager.dto.CompanyDTO;
import org.manager.dto.UserDTO;
import org.manager.enums.Role;
import org.manager.service.CompanyService;
import org.manager.service.UserRegisterService;
import org.manager.util.AlertUtil;

public class UserRegisterController {

    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private CheckBox activeCheckBox;
    @FXML private ComboBox<CompanyDTO> companyComboBox;

    @FXML private Button registerButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button cancelButton;

    @FXML private TableView<UserDTO> userTable;
    @FXML private TableColumn<UserDTO, Long> idColumn;
    @FXML private TableColumn<UserDTO, String> emailColumn;
    @FXML private TableColumn<UserDTO, String> usernameColumn;
    @FXML private TableColumn<UserDTO, Role> roleColumn;
    @FXML private TableColumn<UserDTO, Boolean> activeColumn;
    @FXML private TableColumn<UserDTO, String> companyColumn;

    private final UserRegisterService userService;
    private final CompanyService companyService;
    private final ObservableList<UserDTO> usersData;
    private UserDTO editingUser = null;

    public UserRegisterController() {
        this.userService = new UserRegisterService();
        this.companyService = new CompanyService();
        this.usersData = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        userTable.setItems(usersData);
        loadRoles();
        loadCompanies();
        loadUsers();
        clearForm();

        // Listener para preencher formulário ao clicar na tabela
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                populateForm(newUser);
            }
        });
    }

    // ----------------- BOTÕES -----------------

    @FXML
    public void createUser() {
        if (!validateForm()) return;

        CompanyDTO company = companyComboBox.getValue();
        if (company == null || company.getId() == null) {
            AlertUtil.showError("Erro", "Selecione uma empresa válida.");
            return;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(usernameField.getText());
        userDTO.setEmail(emailField.getText());
        userDTO.setPassword(passwordField.getText());
        userDTO.setRole(roleComboBox.getValue());
        userDTO.setActive(activeCheckBox.isSelected());
        userDTO.setCompanyId(company.getId());

        userService.createUser(userDTO)
                .thenAccept(user -> Platform.runLater(() -> {
                    usersData.add(user);
                    AlertUtil.showInfo("Sucesso", "Usuário cadastrado com sucesso!");
                    clearForm();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void updateUserAction() {
        UserDTO selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            AlertUtil.showError("Erro", "Selecione um usuário para atualizar.");
            return;
        }

        CompanyDTO selectedCompany = companyComboBox.getValue();
        if (selectedCompany == null) {
            AlertUtil.showError("Erro", "Selecione uma empresa antes de atualizar.");
            return;
        }

        selectedUser.setCompanyId(selectedCompany.getId());
        selectedUser.setUsername(usernameField.getText());
        selectedUser.setEmail(emailField.getText());
        selectedUser.setRole(roleComboBox.getValue());
        selectedUser.setActive(activeCheckBox.isSelected());

        userService.updateUser(selectedUser)
                .thenAccept(user -> Platform.runLater(() -> {
                    for (int i = 0; i < usersData.size(); i++) {
                        if (usersData.get(i).getId().equals(user.getId())) {
                            usersData.set(i, user);
                            break;
                        }
                    }
                    AlertUtil.showInfo("Sucesso", "Usuário atualizado com sucesso!");
                    clearForm();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", ex.getMessage()));
                    return null;
                });
    }

    @FXML
    public void deleteUserAction() {
        UserDTO selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            AlertUtil.showError("Aviso", "Selecione um usuário para excluir.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Confirmação",
                "Deseja excluir o usuário: " + selectedUser.getUsername() + "?");

        if (!confirm) return;

        userService.deleteUser(selectedUser.getId())
                .thenRun(() -> Platform.runLater(() -> {
                    usersData.remove(selectedUser);
                    AlertUtil.showInfo("Sucesso", "Usuário removido com sucesso!");
                    clearForm();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", ex.getMessage()));
                    return null;
                });
    }

    @FXML
    public void cancel() {
        clearForm();
    }

    // ----------------- MÉTODOS AUXILIARES -----------------

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
    }

    private void populateForm(UserDTO user) {
        editingUser = user;
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        passwordField.setText(user.getPassword() != null ? user.getPassword() : "");
        roleComboBox.setValue(user.getRole());
        activeCheckBox.setSelected(user.isActive());

        companyComboBox.getItems().stream()
                .filter(c -> c.getId().equals(user.getCompanyId()))
                .findFirst()
                .ifPresent(c -> companyComboBox.getSelectionModel().select(c));
    }

    private void clearForm() {
        editingUser = null;
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
        activeCheckBox.setSelected(false);
        companyComboBox.getSelectionModel().clearSelection();
        userTable.getSelectionModel().clearSelection();
    }

    private void loadRoles() {
        roleComboBox.getItems().setAll(Role.values());
        roleComboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Role role, boolean empty) {
                super.updateItem(role, empty);
                setText(empty || role == null ? null : role.getLabel());
            }
        });
        roleComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Role role, boolean empty) {
                super.updateItem(role, empty);
                setText(empty || role == null ? null : role.getLabel());
            }
        });
    }

    private void loadCompanies() {
        companyService.getAllCompanies()
                .thenAccept(companies -> Platform.runLater(() -> {
                    companyComboBox.getItems().clear();
                    companyComboBox.getItems().addAll(companies);
                    companyComboBox.setCellFactory(cb -> new ListCell<>() {
                        @Override
                        protected void updateItem(CompanyDTO item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? null : item.getName());
                        }
                    });
                    companyComboBox.setButtonCell(new ListCell<>() {
                        @Override
                        protected void updateItem(CompanyDTO item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? null : item.getName());
                        }
                    });
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao carregar empresas: " + ex.getMessage()));
                    return null;
                });
    }

    private void loadUsers() {
        userService.getAllUsers()
                .thenAccept(users -> Platform.runLater(() -> usersData.setAll(users)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao carregar usuários: " + ex.getMessage()));
                    return null;
                });
    }

    private boolean validateForm() {
        if (usernameField.getText().isEmpty() || emailField.getText().isEmpty() ||
                passwordField.getText().isEmpty() || roleComboBox.getValue() == null ||
                companyComboBox.getValue() == null) {
            AlertUtil.showError("Erro", "Preencha todos os campos.");
            return false;
        }
        return true;
    }
}
