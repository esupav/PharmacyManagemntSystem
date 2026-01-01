package org.example.pharmacymanagementsystem;

import com.pharmacy.database.DBconnection;
import com.pharmacy.model.Customer;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Sale;
import com.pharmacy.model.User;
import com.pharmacy.service.DrugInfoService;
import com.pharmacy.util.AuditLogger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.CompletableFuture;

public class HelloController implements UserController {
    private User currentUser;

    @FXML private Label userLabel;
    @FXML private Label dateTimeLabel;
    @FXML private TabPane mainTabPane;

    // Sales Fields
    @FXML private ComboBox<Medicine> saleMedicineCombo;
    @FXML private ComboBox<Customer> saleCustomerCombo;
    @FXML private TextField saleQtyField;
    @FXML private TextField doctorNameField;
    @FXML private Label stockWarningLabel;
    @FXML private TableView<Sale> saleTable;
    @FXML private TableColumn<Sale, String> saleMedCol;
    @FXML private TableColumn<Sale, String> saleCustCol;
    @FXML private TableColumn<Sale, Integer> saleQtyCol;
    @FXML private TableColumn<Sale, Double> salePriceCol;
    @FXML private TableColumn<Sale, Double> saleTotalCol;
    @FXML private TableColumn<Sale, String> saleDateCol;
    @FXML private TableColumn<Sale, String> saleDocCol;
    @FXML private Label totalBillLabel;
    
    // My Daily Sales Report
    @FXML private TableView<Sale> mySalesTable;
    @FXML private TableColumn<Sale, Integer> mySaleIdCol;
    @FXML private TableColumn<Sale, String> mySaleMedCol;
    @FXML private TableColumn<Sale, Integer> mySaleQtyCol;
    @FXML private TableColumn<Sale, Double> mySaleTotalCol;
    @FXML private TableColumn<Sale, String> mySaleDateCol;
    @FXML private Label myDailyTotalLabel;
    
    // Drug Info Panel
    @FXML private Label drugInfoTitle;
    @FXML private TextArea drugInfoArea;

    // Customer Fields
    @FXML private TextField custNameField;
    @FXML private TextField custContactField;
    @FXML private TextField custAddressField;
    @FXML private TextField custAgeField;
    @FXML private ComboBox<String> custGenderCombo;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> custIdCol;
    @FXML private TableColumn<Customer, String> custNameCol;
    @FXML private TableColumn<Customer, String> custContactCol;
    @FXML private TableColumn<Customer, Integer> custAgeCol;
    @FXML private TableColumn<Customer, String> custGenderCol;
    @FXML private TableColumn<Customer, String> custAddressCol;

    private ObservableList<Medicine> medicineList = FXCollections.observableArrayList();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private ObservableList<Sale> saleList = FXCollections.observableArrayList();
    private ObservableList<Sale> myDailySalesList = FXCollections.observableArrayList();
    private DrugInfoService drugInfoService = new DrugInfoService();

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        userLabel.setText("User: " + currentUser.getUsername());
        loadMyDailySales();
    }

    @FXML
    public void initialize() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dateTimeLabel.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        custGenderCombo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        
        setupCustomerTable();
        setupSaleTable();
        setupMySalesTable();
        setupCombos();
        loadData();
    }

    private void setupCombos() {
        saleMedicineCombo.setItems(medicineList);
        saleMedicineCombo.setConverter(new StringConverter<Medicine>() {
            @Override
            public String toString(Medicine object) {
                return object == null ? "" : object.getName() + " (Stock: " + object.getQuantity() + ")";
            }
            @Override
            public Medicine fromString(String string) {
                return null;
            }
        });

        saleCustomerCombo.setItems(customerList);
        saleCustomerCombo.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer object) {
                return object == null ? "" : object.getName();
            }
            @Override
            public Customer fromString(String string) {
                return null;
            }
        });
    }

    private void setupSaleTable() {
        saleCustCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        saleTotalCol.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));
        saleDateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        saleTable.setItems(saleList);
    }
    
    private void setupMySalesTable() {
        mySaleIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        mySaleTotalCol.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));
        mySaleDateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        mySalesTable.setItems(myDailySalesList);
    }

    private void setupCustomerTable() {
        custIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        custNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        custContactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        custAgeCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        custGenderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        custAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        customerTable.setItems(customerList);
    }

    private void loadData() {
        loadMedicines();
        loadCustomers();
        loadSalesHistory();
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

    private void loadCustomers() {
        customerList.clear();
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {
            while (rs.next()) {
                customerList.add(new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("contact"), rs.getString("address"), rs.getInt("age"), rs.getString("gender"), rs.getDouble("credit_balance")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private void loadSalesHistory() {
        saleList.clear();
        String sql = "SELECT s.id, c.name as cust_name, s.total_amount, s.tax_amount, s.final_amount, s.sale_type, s.sale_date, s.pharmacist_username FROM sales s JOIN customers c ON s.customer_id = c.id ORDER BY s.id DESC LIMIT 50";
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                saleList.add(new Sale(rs.getInt("id"), rs.getString("cust_name"), rs.getDouble("total_amount"), rs.getDouble("tax_amount"), rs.getDouble("final_amount"), rs.getString("sale_type"), rs.getTimestamp("sale_date").toLocalDateTime(), rs.getString("pharmacist_username")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private void loadMyDailySales() {
        if (currentUser == null) return;
        myDailySalesList.clear();
        double total = 0.0;
        
        String sql = "SELECT s.id, c.name as cust_name, s.total_amount, s.tax_amount, s.final_amount, s.sale_type, s.sale_date, s.pharmacist_username FROM sales s JOIN customers c ON s.customer_id = c.id WHERE s.pharmacist_username = ? AND DATE(s.sale_date) = CURDATE() ORDER BY s.id DESC";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUser.getUsername());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                double saleTotal = rs.getDouble("final_amount");
                total += saleTotal;
                myDailySalesList.add(new Sale(rs.getInt("id"), rs.getString("cust_name"), rs.getDouble("total_amount"), rs.getDouble("tax_amount"), saleTotal, rs.getString("sale_type"), rs.getTimestamp("sale_date").toLocalDateTime(), rs.getString("pharmacist_username")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        myDailyTotalLabel.setText(String.format("Total Sales: ETB %.2f", total));
    }
    
    @FXML
    protected void onMedicineSelected() {
        Medicine selectedMed = saleMedicineCombo.getValue();
        if (selectedMed != null) {
            drugInfoTitle.setText("Loading info for " + selectedMed.getName() + "...");
            drugInfoArea.clear();
            
            drugInfoService.getDrugDescription(selectedMed.getName())
                .thenAccept(description -> Platform.runLater(() -> {
                    drugInfoTitle.setText(selectedMed.getName());
                    drugInfoArea.setText(description);
                }));
        }
    }

    @FXML
    protected void onAddToBillClick() {
        // This logic is now part of the POSController, this is a simplified version
        showError("This is a simplified view. Use the main POS for sales.");
    }
    
    private void showError(String message) {
        stockWarningLabel.setText(message);
        stockWarningLabel.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #f8d7da; -fx-padding: 5; -fx-background-radius: 3;");
    }
    
    private void showSuccess(String message) {
        stockWarningLabel.setText(message);
        stockWarningLabel.setStyle("-fx-text-fill: #2ecc71; -fx-background-color: #d4edda; -fx-padding: 5; -fx-background-radius: 3;");
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        }
    }

    private void updateStock(int medId, int newQty) {
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE medicines SET quantity = ? WHERE id = ?")) {
            pstmt.setInt(1, newQty);
            pstmt.setInt(2, medId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onAddCustomerClick() {
        String name = custNameField.getText().trim();
        String contact = custContactField.getText().trim();
        String address = custAddressField.getText().trim();
        String ageStr = custAgeField.getText().trim();
        String gender = custGenderCombo.getValue();

        if (name.isEmpty() || contact.isEmpty() || address.isEmpty() || ageStr.isEmpty() || gender == null) {
            showError("All customer fields are required.");
            return;
        }
        
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            showError("Invalid age format.");
            return;
        }

        String sql = "INSERT INTO customers(name, contact, address, age, gender) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setString(3, address);
            pstmt.setInt(4, age);
            pstmt.setString(5, gender);
            pstmt.executeUpdate();

            custNameField.clear(); custContactField.clear(); custAddressField.clear(); custAgeField.clear();
            custGenderCombo.getSelectionModel().clearSelection();
            loadCustomers();
            showSuccess("Customer Added!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    protected void onLogoutClick() {
        try {
            Stage stage = (Stage) userLabel.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setTitle("Pharmacy Login");
            stage.centerOnScreen();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
