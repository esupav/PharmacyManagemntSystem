package org.example.pharmacymanagementsystem;

import com.pharmacy.database.DBconnection;
import com.pharmacy.model.User;
import com.pharmacy.util.AuditLogger;
import com.pharmacy.util.PasswordUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    protected void onLoginClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required.");
            return;
        }

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (PasswordUtil.checkPassword(password, hashedPassword)) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            hashedPassword,
                            rs.getString("role")
                    );
                    
                    AuditLogger.log(username, AuditLogger.ActionType.LOGIN_SUCCESS, "User logged in successfully.");
                    loadMainApplication(user);
                } else {
                    AuditLogger.log(username, AuditLogger.ActionType.LOGIN_FAIL, "Attempted login with invalid credentials.");
                    errorLabel.setText("Invalid username or password.");
                }
            } else {
                AuditLogger.log(username, AuditLogger.ActionType.LOGIN_FAIL, "Attempted login with invalid credentials.");
                errorLabel.setText("Invalid username or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Database connection error.");
        }
    }

    private void loadMainApplication(User user) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setUser(user);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Pharmacy Management System");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    protected void onBackClick() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("welcome-view.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Pharmacy Management System");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
