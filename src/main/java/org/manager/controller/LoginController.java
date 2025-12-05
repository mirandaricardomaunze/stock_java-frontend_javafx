package org.manager.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.manager.session.SessionManager;
import org.manager.dto.LoginRequestDTO;
import org.manager.service.UserLoginService;
import org.manager.util.AlertUtil;

public class LoginController {

    private final UserLoginService userLoginService;
   @FXML
    private Button loginButton;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    public LoginController() {
        this.userLoginService = new UserLoginService();
    }

    @FXML
    private void initialize() {
        loginButton.setDefaultButton(true);
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("Erro", "Preencha todos os campos.");
            return;
        }
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(email, password);

        userLoginService.handleLogin(loginRequestDTO)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response != null && response.getToken() != null && response.getCompanyId() != null && response.getRole() != null) {
                        SessionManager.setToken(response.getToken());
                        SessionManager.setCurrentUser(
                                response.getUsername() != null ? response.getUsername() : response.getEmail()
                        );
                        SessionManager.setCurrentCompanyId(response.getCompanyId());
                        SessionManager.setCurrentRole(response.getRole());
                        SessionManager.setCurrentUserId(response.getUserId());
                        System.out.println("Usuário logado: " + SessionManager.getCurrentUser());
                        System.out.println("TOKEN JWT: " + SessionManager.getToken());
                        System.out.println("Empresa ID: " + SessionManager.getCurrentCompanyId());
                        System.out.println("Role: " + SessionManager.getCurrentRole());
                        System.out.println("UserId: " + SessionManager.getCurrentUserId());


                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
                            Parent root = loader.load();
                            Stage stage = new Stage();
                            stage.setTitle("Painel Principal");
                            stage.setScene(new Scene(root));
                            stage.setMaximized(true);
                            stage.show();

                            Stage currentStage = (Stage) emailField.getScene().getWindow();
                            currentStage.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            AlertUtil.showError("Erro", "Falha ao abrir a tela principal.");
                        }
                    } else {
                        AlertUtil.showError("Erro", "Usuário ou senha inválidos!");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            AlertUtil.showError("Erro", "Falha ao conectar com o servidor: " + ex.getMessage()));
                    return null;
                });
    }

}
