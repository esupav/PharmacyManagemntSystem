package org.example.pharmacymanagementsystem;

import com.pharmacy.database.DBconnection;
import com.pharmacy.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsController implements UserController {

    @FXML private TextField taxRateField;
    @FXML private TextField lowStockThresholdField;
    @FXML private TextField expiryAlertPeriodField;
    @FXML private Label settingsMessageLabel;

    @Override
    public void setUser(User user) {
        // The current user is not needed in this controller, but the method must be implemented.
    }

    @FXML
    public void initialize() {
        loadSettings();
    }

    private void loadSettings() {
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT setting_key, setting_value FROM settings")) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String key = rs.getString("setting_key");
                String value = rs.getString("setting_value");
                switch (key) {
                    case "tax_rate":
                        taxRateField.setText(String.valueOf(Double.parseDouble(value) * 100));
                        break;
                    case "low_stock_threshold":
                        lowStockThresholdField.setText(value);
                        break;
                    case "expiry_alert_period":
                        expiryAlertPeriodField.setText(value);
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveSettings() {
        if (updateSetting("tax_rate", taxRateField.getText(), true) &&
            updateSetting("low_stock_threshold", lowStockThresholdField.getText(), false) &&
            updateSetting("expiry_alert_period", expiryAlertPeriodField.getText(), false)) {
            showMessage("Settings updated successfully.", false);
        }
    }

    private boolean updateSetting(String key, String value, boolean isPercentage) {
        String valueToSave = value;
        if (value.trim().isEmpty()) {
            showMessage(key.replace("_", " ") + " cannot be empty.", true);
            return false;
        }
        try {
            double numericValue = Double.parseDouble(value);
            if (isPercentage) {
                valueToSave = String.valueOf(numericValue / 100.0);
            }
        } catch (NumberFormatException e) {
            showMessage(key.replace("_", " ") + " must be a valid number.", true);
            return false;
        }

        String sql = "UPDATE settings SET setting_value = ? WHERE setting_key = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, valueToSave);
            pstmt.setString(2, key);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            showMessage("Failed to update " + key.replace("_", " ") + ".", true);
            e.printStackTrace();
            return false;
        }
    }

    private void showMessage(String message, boolean isError) {
        settingsMessageLabel.setText(message);
        settingsMessageLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
        settingsMessageLabel.setVisible(true);
        settingsMessageLabel.setManaged(true);
    }
}
