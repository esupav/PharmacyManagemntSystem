package org.example.pharmacymanagementsystem;

import com.pharmacy.model.User;
import com.pharmacy.util.AuditLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private VBox menuContainer;
    @FXML private BorderPane contentPane;
    @FXML private Label viewTitleLabel;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;

    private User currentUser;
    private final Map<String, String> adminMenu = new HashMap<>() {{
        put("Dashboard", "admin-dashboard.fxml");
        put("Inventory", "inventory-view.fxml");
        put("Reports", "reports-view.fxml");
        put("Users", "users-view.fxml");
        put("Settings", "settings-view.fxml");
    }};

    private final Map<String, String> pharmacistMenu = new HashMap<>() {{
        put("POS", "pos-view.fxml");
        put("Credit Sales", "credit-sales-view.fxml");
        put("My Daily Report", "daily-report-view.fxml");
    }};

    public void setUser(User user) {
        this.currentUser = user;
        usernameLabel.setText(user.getUsername());
        roleLabel.setText(user.getRole());
        buildMenu();
        
        if ("ADMIN".equals(user.getRole())) {
            loadView("Dashboard", adminMenu.get("Dashboard"));
        } else {
            loadView("POS", pharmacistMenu.get("POS"));
        }
    }

    private void buildMenu() {
        menuContainer.getChildren().clear();
        Map<String, String> menuItems = "ADMIN".equals(currentUser.getRole()) ? adminMenu : pharmacistMenu;

        for (Map.Entry<String, String> entry : menuItems.entrySet()) {
            Button menuButton = new Button(entry.getKey());
            menuButton.setMaxWidth(Double.MAX_VALUE);
            menuButton.getStyleClass().add("menu-button");
            menuButton.setOnAction(event -> loadView(entry.getKey(), entry.getValue()));
            menuContainer.getChildren().add(menuButton);
        }
    }

    private void loadView(String title, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();

            // This is the crucial part: pass the user to the new controller
            Object controller = loader.getController();
            if (controller instanceof UserController) {
                ((UserController) controller).setUser(currentUser);
            }

            contentPane.setCenter(view);
            viewTitleLabel.setText(title);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load view: " + fxmlFile);
        }
    }

    @FXML
    private void onLogout() {
        AuditLogger.log(currentUser.getUsername(), AuditLogger.ActionType.LOGOUT, "User logged out successfully.");
        try {
            Stage stage = (Stage) menuContainer.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Pharmacy Management System");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * An interface that allows the MainController to pass the logged-in user 
 * to any controller that needs it.
 */
interface UserController {
    void setUser(User user);
}
