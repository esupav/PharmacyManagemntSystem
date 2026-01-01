package org.example.pharmacymanagementsystem;

import com.pharmacy.database.DBconnection;
import com.pharmacy.model.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AddCustomerController {

    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private TextField addressField;
    @FXML private TextField ageField;
    @FXML private TextField genderField;
    @FXML private Label errorLabel;

    private POSController posController;
    private Customer newCustomer;

    public void setPosController(POSController posController) {
        this.posController = posController;
    }

    public Customer getNewCustomer() {
        return newCustomer;
    }

    @FXML
    private void onSave() {
        // 1. Validation
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Customer name is required.");
            return;
        }

        String contact = contactField.getText().trim();
        String address = addressField.getText().trim();
        String gender = genderField.getText().trim();
        String ageText = ageField.getText().trim();
        int age = 0;

        if (!ageText.isEmpty()) {
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException e) {
                showError("Age must be a valid number.");
                return;
            }
        }

        // 2. Database Operation
        try (Connection conn = DBconnection.getConnection()) {
            if (customerExists(conn, name)) {
                showError("A customer with this name already exists.");
                return;
            }

            String sql = "INSERT INTO customers (name, contact, address, age, gender) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setString(2, contact);
                pstmt.setString(3, address);
                pstmt.setInt(4, age);
                pstmt.setString(5, gender);
                
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating customer failed, no rows affected.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        newCustomer = new Customer(id, name, contact, address, age, gender, 0.0);
                        
                        if (posController != null) {
                            posController.addCustomerToList(newCustomer);
                        }
                        
                        closeStage();
                    } else {
                        throw new SQLException("Creating customer failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            showError("Database error. Could not save customer.");
            e.printStackTrace();
        }
    }

    private boolean customerExists(Connection conn, String name) throws SQLException {
        String checkSql = "SELECT id FROM customers WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @FXML
    private void onCancel() {
        closeStage();
    }

    private void closeStage() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
