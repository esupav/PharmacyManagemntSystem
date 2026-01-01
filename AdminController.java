package org.example.pharmacymanagementsystem;

import com.pharmacy.database.DBconnection;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.PharmacistPerformance;
import com.pharmacy.model.Sale;
import com.pharmacy.model.Supplier;
import com.pharmacy.model.User;
import com.pharmacy.util.AuditLogger;
import com.pharmacy.util.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class AdminController implements UserController {
    private User currentUser;

    @FXML private Label userLabel;
    @FXML private Label totalMedsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label expiredMedsLabel;
    @FXML private Label totalSalesLabel;

    // Charts
    @FXML private LineChart<String, Number> salesTrendChart;
    @FXML private BarChart<String, Number> pharmacistSalesChart;

    // Medicine Fields
    @FXML private TextField medNameField, medCompanyField, medBatchField, medQtyField, medPriceField, searchMedField;
    @FXML private DatePicker medExpiryField;
    @FXML private CheckBox medPrescriptionCheck;
    @FXML private TableView<Medicine> medicineTable;
    @FXML private TableColumn<Medicine, Integer> medIdCol;
    @FXML private TableColumn<Medicine, String> medNameCol, medCompanyCol, medBatchCol, medExpiryCol;
    @FXML private TableColumn<Medicine, Integer> medQtyCol;
    @FXML private TableColumn<Medicine, Double> medPriceCol;
    @FXML private TableColumn<Medicine, Boolean> medRxCol;
    @FXML private Label medFormErrorLabel;

    // Sales Report Fields
    @FXML private TableView<Sale> fullSaleReportTable;
    @FXML private TableColumn<Sale, Integer> reportSaleIdCol, reportQtyCol;
    @FXML private TableColumn<Sale, String> reportMedNameCol, reportCustNameCol, reportDateCol, reportPharmacistCol, reportDoctorCol;
    @FXML private TableColumn<Sale, Double> reportPriceCol;

    // Pharmacist Performance Fields
    @FXML private TableView<PharmacistPerformance> pharmacistPerformanceTable;
    @FXML private TableColumn<PharmacistPerformance, String> perfPharmacistCol;
    @FXML private TableColumn<PharmacistPerformance, Double> perfTotalSalesCol;
    @FXML private TableColumn<PharmacistPerformance, Integer> perfNumSalesCol;

    // Supplier Fields
    @FXML private TextField supNameField, supContactField, supAddressField, supLicenseField;
    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, Integer> supIdCol;
    @FXML private TableColumn<Supplier, String> supNameCol, supContactCol, supAddressCol, supLicenseCol;
    @FXML private Label supFormErrorLabel;

    // User Management
    @FXML private TextField regUsernameField, regPasswordField;
    @FXML private Label regMessageLabel;
    
    // Buttons
    @FXML private Button btnAddMed, btnUpdateMed, btnDeleteMed, btnClearMed;
    @FXML private Button btnAddSupplier;
    @FXML private Button btnRegisterUser;
    @FXML private Button btnLogout;

    private ObservableList<Medicine> medicineList = FXCollections.observableArrayList();
    private FilteredList<Medicine> filteredMedicineList;
    private ObservableList<Supplier> supplierList = FXCollections.observableArrayList();
    private ObservableList<Sale> fullSaleList = FXCollections.observableArrayList();
    private ObservableList<PharmacistPerformance> performanceList = FXCollections.observableArrayList();
    private Medicine selectedMedicine = null;

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        userLabel.setText("User: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
    }

    @FXML
    public void initialize() {
        setupIcons();
        setupAllTables();
        loadAllData();
        
        medicineTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            selectedMedicine = val;
            if (val != null) populateMedicineForm(val);
        });
    }
    
    private void setupIcons() {
        // Medicine Buttons
        if (btnAddMed != null) {
            btnAddMed.setGraphic(new FontIcon(FontAwesomeSolid.PLUS));
            btnAddMed.setText("Add");
        }
        if (btnUpdateMed != null) {
            btnUpdateMed.setGraphic(new FontIcon(FontAwesomeSolid.PENCIL_ALT));
            btnUpdateMed.setText("Update");
        }
        if (btnDeleteMed != null) {
            btnDeleteMed.setGraphic(new FontIcon(FontAwesomeSolid.TRASH));
            btnDeleteMed.setText("Delete");
        }
        if (btnClearMed != null) {
            btnClearMed.setGraphic(new FontIcon(FontAwesomeSolid.ERASER));
            btnClearMed.setText("Clear");
        }
        
        // Supplier Button
        if (btnAddSupplier != null) {
            btnAddSupplier.setGraphic(new FontIcon(FontAwesomeSolid.TRUCK));
            btnAddSupplier.setText("Add Supplier");
        }
        
        // User Button
        if (btnRegisterUser != null) {
            btnRegisterUser.setGraphic(new FontIcon(FontAwesomeSolid.USER_PLUS));
            btnRegisterUser.setText("Register");
        }
        
        // Logout Button
        if (btnLogout != null) {
            FontIcon logoutIcon = new FontIcon(FontAwesomeSolid.SIGN_OUT_ALT);
            logoutIcon.setIconSize(20);
            btnLogout.setGraphic(logoutIcon);
            btnLogout.setText("");
            btnLogout.setTooltip(new Tooltip("Logout"));
        }
    }

    private void setupAllTables() {
        // Medicine Table
        medIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        medNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        medCompanyCol.setCellValueFactory(new PropertyValueFactory<>("company"));
        medBatchCol.setCellValueFactory(new PropertyValueFactory<>("batchNumber"));
        medQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        medPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        medExpiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        medRxCol.setCellValueFactory(new PropertyValueFactory<>("requiresPrescription"));
        
        filteredMedicineList = new FilteredList<>(medicineList, p -> true);
        medicineTable.setItems(filteredMedicineList);

        // Add row factory for highlighting
        medicineTable.setRowFactory(tv -> new TableRow<Medicine>() {
            @Override
            protected void updateItem(Medicine item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                    return;
                }

                LocalDate expiryDate = null;
                try {
                    expiryDate = LocalDate.parse(item.getExpiryDate());
                } catch (DateTimeParseException e) {
                    // If date is invalid, don't style
                    setStyle("");
                    return;
                }

                // Priority 1: Expired
                if (expiryDate.isBefore(LocalDate.now())) {
                    setStyle("-fx-background-color: #f8d7da;"); // Light Red
                } 
                // Priority 2: Nearing Expiry
                else if (expiryDate.isBefore(LocalDate.now().plusMonths(3))) {
                    setStyle("-fx-background-color: #fff3cd;"); // Light Yellow/Orange
                } 
                // Priority 3: Low Stock
                else if (item.getQuantity() < 10) {
                    setStyle("-fx-background-color: #e2e3e5;"); // Light Grey/Blue
                } 
                // Default: No special styling
                else {
                    setStyle("");
                }
            }
        });

        // Sales Report Table
        reportSaleIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        reportCustNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        reportPriceCol.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));
        reportDateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        reportPharmacistCol.setCellValueFactory(new PropertyValueFactory<>("pharmacistUsername"));
        fullSaleReportTable.setItems(fullSaleList);

        // Performance Table
        perfPharmacistCol.setCellValueFactory(new PropertyValueFactory<>("pharmacistUsername"));
        perfTotalSalesCol.setCellValueFactory(new PropertyValueFactory<>("totalSales"));
        perfNumSalesCol.setCellValueFactory(new PropertyValueFactory<>("numberOfSales"));
        pharmacistPerformanceTable.setItems(performanceList);

        // Supplier Table
        supIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        supNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        supContactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        supAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        supLicenseCol.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        supplierTable.setItems(supplierList);
    }

    private void loadAllData() {
        loadMedicines();
        loadSuppliers();
        loadFullSalesReport();
        loadPharmacistPerformance();
        updateDashboardStats();
        loadChartData();
    }

    private void updateDashboardStats() {
        totalMedsLabel.setText(String.valueOf(medicineList.size()));
        lowStockLabel.setText(String.valueOf(medicineList.stream().filter(m -> m.getQuantity() < 10).count()));
        expiredMedsLabel.setText(String.valueOf(medicineList.stream().filter(m -> {
            try {
                return LocalDate.parse(m.getExpiryDate()).isBefore(LocalDate.now());
            } catch (DateTimeParseException e) {
                return false;
            }
        }).count()));
        
        double totalSales = 0.0;
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT SUM(final_amount) FROM sales WHERE DATE(sale_date) = CURDATE()")) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) totalSales = rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        totalSalesLabel.setText(String.format("ETB %.2f", totalSales));
    }

    private void loadChartData() {
        salesTrendChart.getData().clear();
        pharmacistSalesChart.getData().clear();

        XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();
        salesSeries.setName("Daily Sales");
        String salesSQL = "SELECT DATE(sale_date) as day, SUM(final_amount) as daily_total FROM sales WHERE sale_date >= ? GROUP BY day ORDER BY day ASC";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(salesSQL)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now().minusDays(7)));
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                salesSeries.getData().add(new XYChart.Data<>(rs.getString("day"), rs.getDouble("daily_total")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        salesTrendChart.getData().add(salesSeries);

        XYChart.Series<String, Number> performanceSeries = new XYChart.Series<>();
        performanceSeries.setName("Sales by Pharmacist");
        for (PharmacistPerformance p : performanceList) {
            performanceSeries.getData().add(new XYChart.Data<>(p.getPharmacistUsername(), p.getTotalSales()));
        }
        pharmacistSalesChart.getData().add(performanceSeries);
    }

    private void loadMedicines() {
        medicineList.clear();
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM medicines")) {
            while (rs.next()) {
                medicineList.add(new Medicine(rs.getInt("id"), rs.getString("name"), rs.getString("company"), rs.getString("batch_number"), rs.getInt("quantity"), rs.getDouble("price"), rs.getString("expiry_date"), rs.getBoolean("requires_prescription")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadFullSalesReport() {
        fullSaleList.clear();
        String sql = "SELECT s.id, c.name as cust_name, s.total_amount, s.tax_amount, s.final_amount, s.sale_type, s.sale_date, s.pharmacist_username FROM sales s JOIN customers c ON s.customer_id = c.id ORDER BY s.id DESC";
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                fullSaleList.add(new Sale(rs.getInt("id"), rs.getString("cust_name"), rs.getDouble("total_amount"), rs.getDouble("tax_amount"), rs.getDouble("final_amount"), rs.getString("sale_type"), rs.getTimestamp("sale_date").toLocalDateTime(), rs.getString("pharmacist_username")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private void loadPharmacistPerformance() {
        performanceList.clear();
        String sql = "SELECT pharmacist_username, SUM(final_amount) as total_sales, COUNT(*) as num_sales FROM sales GROUP BY pharmacist_username";
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                performanceList.add(new PharmacistPerformance(rs.getString("pharmacist_username"), rs.getDouble("total_sales"), rs.getInt("num_sales")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadSuppliers() {
        supplierList.clear();
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM suppliers")) {
            while (rs.next()) {
                supplierList.add(new Supplier(rs.getInt("id"), rs.getString("name"), rs.getString("contact"), rs.getString("address"), rs.getString("license_number")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    @FXML
    protected void onSearchMedicine() {
        String filter = searchMedField.getText().toLowerCase();
        filteredMedicineList.setPredicate(med -> med.getName().toLowerCase().contains(filter) || med.getCompany().toLowerCase().contains(filter));
    }

    @FXML
    protected void onAddMedicineClick() {
        if (!validateMedicineForm()) return;
        
        String sql = "INSERT INTO medicines(name, company, batch_number, quantity, price, expiry_date, requires_prescription) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParams(pstmt);
            pstmt.executeUpdate();
            AuditLogger.log(currentUser.getUsername(), AuditLogger.ActionType.MEDICINE_ADDED, "Added: " + medNameField.getText());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Medicine added successfully.");
            loadAllData();
            onClearMedicineForm();
        } catch (SQLException | NumberFormatException e) {
            showMedFormError("Failed to add medicine. Please check your input.");
            e.printStackTrace();
        }
    }
    
    @FXML
    protected void onUpdateMedicineClick() {
        if (selectedMedicine == null) {
            showMedFormError("Please select a medicine from the table to update.");
            return;
        }
        if (!validateMedicineForm()) return;

        String sql = "UPDATE medicines SET name=?, company=?, batch_number=?, quantity=?, price=?, expiry_date=?, requires_prescription=? WHERE id=?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParams(pstmt);
            pstmt.setInt(8, selectedMedicine.getId());
            pstmt.executeUpdate();
            AuditLogger.log(currentUser.getUsername(), AuditLogger.ActionType.MEDICINE_UPDATED, "Updated ID: " + selectedMedicine.getId());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Medicine updated successfully.");
            loadAllData();
            onClearMedicineForm();
        } catch (SQLException | NumberFormatException e) {
            showMedFormError("Failed to update medicine. Please check your input.");
            e.printStackTrace();
        }
    }
    
    @FXML
    protected void onDeleteMedicineClick() {
        if (selectedMedicine == null) {
            showMedFormError("Please select a medicine from the table to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete " + selectedMedicine.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String sql = "DELETE FROM medicines WHERE id = ?";
                try (Connection conn = DBconnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, selectedMedicine.getId());
                    pstmt.executeUpdate();
                    AuditLogger.log(currentUser.getUsername(), AuditLogger.ActionType.MEDICINE_DELETED, "Deleted ID: " + selectedMedicine.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Medicine deleted successfully.");
                    loadAllData();
                    onClearMedicineForm();
                } catch (SQLException e) {
                    showMedFormError("Could not delete medicine. It might be part of an existing sale record.");
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    protected void onAddSupplierClick() {
        if (!validateSupplierForm()) return;

        String sql = "INSERT INTO suppliers(name, contact, address, license_number) VALUES(?,?,?,?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supNameField.getText().trim());
            pstmt.setString(2, supContactField.getText().trim());
            pstmt.setString(3, supAddressField.getText().trim());
            pstmt.setString(4, supLicenseField.getText().trim());
            pstmt.executeUpdate();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier added successfully.");
            loadSuppliers(); // Refresh the supplier table
            
            // Clear form fields
            supNameField.clear(); 
            supContactField.clear(); 
            supAddressField.clear(); 
            supLicenseField.clear();
            hideSupFormError();
        } catch (SQLException e) {
            showSupFormError("Failed to add supplier. A supplier with this name may already exist.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void onRegisterUserClick() {
        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            showRegMessage("All fields are required.", true);
            return;
        }
        String hashedPassword = PasswordUtil.hashPassword(password);
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'PHARMACIST')";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            showRegMessage("Pharmacist registered successfully!", false);
            AuditLogger.log(currentUser.getUsername(), AuditLogger.ActionType.USER_CREATED, "Created pharmacist: " + username);
            regUsernameField.clear(); regPasswordField.clear();
        } catch (SQLException e) {
            showRegMessage("Username may already exist.", true);
            e.printStackTrace();
        }
    }
    
    @FXML
    protected void onClearMedicineForm() {
        selectedMedicine = null;
        medNameField.clear();
        medCompanyField.clear();
        medBatchField.clear();
        medQtyField.clear();
        medPriceField.clear();
        medExpiryField.setValue(null);
        medPrescriptionCheck.setSelected(false);
        medicineTable.getSelectionModel().clearSelection();
        hideMedFormError();
    }
    
    private void populateMedicineForm(Medicine med) {
        medNameField.setText(med.getName());
        medCompanyField.setText(med.getCompany());
        medBatchField.setText(med.getBatchNumber());
        medQtyField.setText(String.valueOf(med.getQuantity()));
        medPriceField.setText(String.valueOf(med.getPrice()));
        try {
            medExpiryField.setValue(LocalDate.parse(med.getExpiryDate()));
        } catch (DateTimeParseException e) {
            medExpiryField.setValue(null);
        }
        medPrescriptionCheck.setSelected(med.isRequiresPrescription());
    }
    
    private void setStatementParams(PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, medNameField.getText().trim());
        pstmt.setString(2, medCompanyField.getText().trim());
        pstmt.setString(3, medBatchField.getText().trim());
        pstmt.setInt(4, Integer.parseInt(medQtyField.getText()));
        pstmt.setDouble(5, Double.parseDouble(medPriceField.getText()));
        pstmt.setString(6, medExpiryField.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE));
        pstmt.setBoolean(7, medPrescriptionCheck.isSelected());
    }

    private boolean validateMedicineForm() {
        if (medNameField.getText().trim().isEmpty()) {
            showMedFormError("Medicine name is required.");
            return false;
        }
        try {
            Integer.parseInt(medQtyField.getText());
            Double.parseDouble(medPriceField.getText());
        } catch (NumberFormatException e) {
            showMedFormError("Quantity and Price must be valid numbers.");
            return false;
        }
        if (medExpiryField.getValue() == null) {
            showMedFormError("Expiry date is required.");
            return false;
        }
        if (medExpiryField.getValue().isBefore(LocalDate.now())) {
            showMedFormError("Expiry date cannot be in the past.");
            return false;
        }
        hideMedFormError();
        return true;
    }

    private boolean validateSupplierForm() {
        String name = supNameField.getText().trim();
        String contact = supContactField.getText().trim();

        if (name.isEmpty()) {
            showSupFormError("Supplier name is required.");
            return false;
        }
        if (!contact.isEmpty() && !contact.matches("\\d{10}")) {
            showSupFormError("Contact must be a 10-digit number (e.g., 0912345678).");
            return false;
        }
        hideSupFormError();
        return true;
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showMedFormError(String message) {
        medFormErrorLabel.setText(message);
        medFormErrorLabel.setVisible(true);
        medFormErrorLabel.setManaged(true);
    }

    private void hideMedFormError() {
        medFormErrorLabel.setVisible(false);
        medFormErrorLabel.setManaged(false);
    }

    private void showSupFormError(String message) {
        supFormErrorLabel.setText(message);
        supFormErrorLabel.setVisible(true);
        supFormErrorLabel.setManaged(true);
    }

    private void hideSupFormError() {
        supFormErrorLabel.setVisible(false);
        supFormErrorLabel.setManaged(false);
    }

    private void showRegMessage(String message, boolean isError) {
        regMessageLabel.setText(message);
        regMessageLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
        regMessageLabel.setVisible(true);
        regMessageLabel.setManaged(true);
    }

    @FXML
    protected void onLogoutClick() {
        try {
            Stage stage = (Stage) userLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
