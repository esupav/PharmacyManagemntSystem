package org.example.pharmacymanagementsystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {

    @FXML
    protected void onLoginClick() {
        loadView("login-view.fxml", "Selam Pharmacy Login");
    }

    @FXML
    protected void onRegisterClick() {
        loadView("register-view.fxml", "Register New User");
    }

    private void loadView(String fxmlFile, String title) {
        try {
            // Get current stage from any node, here we assume the button triggered it
            // But since we don't have direct reference to button in this method signature, 
            // we'll rely on the fact that this controller is loaded into a stage.
            // A cleaner way is to get the stage from the event source, but for simplicity:
            Stage stage = HelloApplication.primaryStage; 
            if (stage == null) return; // Should not happen if set in Main

            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
