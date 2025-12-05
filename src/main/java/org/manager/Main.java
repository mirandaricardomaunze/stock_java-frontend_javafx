package org.manager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.WHITE); // Aplique a cor antes de mostrar
        stage.setTitle("Sistema de Gerenciamento de Vendas");
        stage.setScene(scene);      // Certifique-se de definir a cena
        stage.setMaximized(true);
        stage.show();               // Só depois mostrar a janela
    }

    public static void main(String[] args) {
        launch(args);
    }
}