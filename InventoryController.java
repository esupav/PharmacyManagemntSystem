package org.example.pharmacymanagementsystem;

import com.pharmacy.database.DBconnection;
import com.pharmacy.model.Medicine;
import com.pharmacy.util.AuditLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.util.Optional;

public class InventoryController {

    @FXML private TextField nameField;
    @FXML private TextField companyField;
    @FXML private TextField batchField;
    @FXML private TextField qtyField;
    @FXML private TextField priceField;
    @FXML private TextField expiryField;
    @FXML private CheckBox prescriptionCheck;
    @FXML private TextField searchField;
    @FXML private TableView<Medicine> medicineTable;
    @FXML private TableColumn<Medicine, Integer> idCol;
    @FXML private TableColumn<Medicine, String> nameCol;
    @FXML private TableColumn<Medicine, String> companyCol;
    @FXML private TableColumn<Medicine, String> batchCol;
    @FXML private TableColumn<Medicine, Integer> qtyCol;
    @FXML private TableColumn<Medicine, Double> priceCol;
    @FXML private TableColumn<Medicine, String> expiryCol;
    @FXML private TableColumn<Medicine, Boolean> rxCol;

    private ObservableList<Medicine> medicineList = FXCollections.observableArrayList();
    private FilteredList<Medicine> filteredMedicineList;
    private Medicine selectedMedicine = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadMedicines();

        // Listener to populate form when a medicine is selected from the table
        medicineTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedMedicine = newSelection;
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredMedicineList.setPredicate(medicine -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newVal.toLowerCase();
                return medicine.getName().toLowerCase().contains(lowerCaseFilter) ||
                       medicine.getCompany().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        companyCol.setCellValueFactory(new PropertyValueFactory<>("company"));
        batchCol.setCellValueFactory(new PropertyValueFactory<>("batchNumber"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        expiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        rxCol.setCellValueFactory(new PropertyValueFactory<>("requiresPrescription"));
    }

    private void loadMedicines() {
        medicineList.clear();
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM medicines")) {
            while (rs.next()) {
                medicineList.add(new Medicine(
                        rs.getInt("id"), rs.getString("name"), rs.getString("company"),
                        rs.getString("batch_number"), rs.getInt("quantity"), rs.getDouble("price"),
                        rs.getString("expiry_date"), rs.getBoolean("requires_prescription")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        filteredMedicineList = new FilteredList<>(medicineList, p -> true);
        medicineTable.setItems(filteredMedicineList);
    }

    @FXML
    private void onAdd() {
        if (!validateFields()) return;

        String sql = "INSERT INTO medicines(name, company, batch_number, quantity, price, expiry_date, requires_prescription) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParams(pstmt);
            pstmt.executeUpdate();

            AuditLogger.log("admin", AuditLogger.ActionType.MEDICINE_ADDED, "Added: " + nameField.getText());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Medicine added successfully.");
            refreshData();
        } catch (SQLException | NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add medicine.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onUpdate() {
        if (selectedMedicine == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a medicine to update.");
            return;
        }
        if (!validateFields()) return;

        String sql = "UPDATE medicines SET name = ?, company = ?, batch_number = ?, quantity = ?, price = ?, expiry_date = ?, requires_prescription = ? WHERE id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParams(pstmt);
            pstmt.setInt(8, selectedMedicine.getId());
            pstmt.executeUpdate();

            AuditLogger.log("admin", AuditLogger.ActionType.MEDICINE_UPDATED, "Updated ID: " + selectedMedicine.getId());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Medicine updated successfully.");
            refreshData();
        } catch (SQLException | NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update medicine.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onDelete() {
        if (selectedMedicine == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a medicine to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this medicine?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "DELETE FROM medicines WHERE id = ?";
            try (Connection conn = DBconnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedMedicine.getId());
                pstmt.executeUpdate();

                AuditLogger.log("admin", AuditLogger.ActionType.MEDICINE_DELETED, "Deleted ID: " + selectedMedicine.getId() + ", Name: " + selectedMedicine.getName());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Medicine deleted successfully.");
                refreshData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete medicine. It may be linked to existing sales records.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onClearForm() {
        nameField.clear();
        companyField.clear();
        batchField.clear();
        qtyField.clear();
        priceField.clear();
        expiryField.clear();
        prescriptionCheck.setSelected(false);
        medicineTable.getSelectionModel().clearSelection();
        selectedMedicine = null;
    }

    private void populateForm(Medicine medicine) {
        nameField.setText(medicine.getName());
        companyField.setText(medicine.getCompany());
        batchField.setText(medicine.getBatchNumber());
        qtyField.setText(String.valueOf(medicine.getQuantity()));
        priceField.setText(String.valueOf(medicine.getPrice()));
        expiryField.setText(medicine.getExpiryDate());
        prescriptionCheck.setSelected(medicine.isRequiresPrescription());
    }

    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty() || qtyField.getText().trim().isEmpty() || priceField.getText().trim().isEmpty() || expiryField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Name, Quantity, Price, and Expiry Date are required fields.");
            return false;
        }
        try {
            Integer.parseInt(qtyField.getText());
            Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity and Price must be valid numbers.");
            return false;
        }
        return true;
    }

    private void setStatementParams(PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, nameField.getText().trim());
        pstmt.setString(2, companyField.getText().trim());
        pstmt.setString(3, batchField.getText().trim());
        pstmt.setInt(4, Integer.parseInt(qtyField.getText()));
        pstmt.setDouble(5, Double.parseDouble(priceField.getText()));
        pstmt.setString(6, expiryField.getText().trim());
        pstmt.setBoolean(7, prescriptionCheck.isSelected());
    }

    private void refreshData() {
        loadMedicines();
        onClearForm();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
