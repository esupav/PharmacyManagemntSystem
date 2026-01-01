package org.example.pharmacymanagementsystem;

import com.pharmacy.database.DBconnection;
import com.pharmacy.util.AuditLogger;
import com.pharmacy.util.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "PHARMACIST"));
    }

    @FXML
    protected void onRegisterClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            messageLabel.setText("Please fill all fields!");
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role);
            pstmt.executeUpdate();

            AuditLogger.log(username, AuditLogger.ActionType.USER_CREATED, "New user registered: " + username + " as " + role);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Registration Successful!");

            // Clear fields
            usernameField.clear();
            passwordField.clear();
            roleCombo.getSelectionModel().clearSelection();

        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Registration Failed! Username may exist.");
        }
    }

    @FXML
    protected void onBackToWelcomeClick() {
        try {
            Stage stage = HelloApplication.primaryStage;
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("welcome-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setTitle("Pharmacy Management System");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
