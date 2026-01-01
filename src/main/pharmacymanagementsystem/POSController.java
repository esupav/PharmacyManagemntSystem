package org.example.pharmacymanagementsystem;

import com.pharmacy.database.DBconnection;
import com.pharmacy.model.Customer;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Sale;
import com.pharmacy.model.SaleItem;
import com.pharmacy.model.User;
import com.pharmacy.util.AuditLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class POSController implements UserController {

    @FXML private ComboBox<Medicine> medicineCombo;
    @FXML private TextField qtyField;
    @FXML private TableView<SaleItem> cartTable;
    @FXML private TableColumn<SaleItem, String> cartMedNameCol;
    @FXML private TableColumn<SaleItem, Integer> cartQtyCol;
    @FXML private TableColumn<SaleItem, Double> cartPriceCol;
    @FXML private TableColumn<SaleItem, Double> cartTotalCol;

    @FXML private ComboBox<User> pharmacistCombo;
    @FXML private ComboBox<Customer> customerCombo;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private RadioButton cashRadio;
    @FXML private RadioButton creditRadio;
    @FXML private VBox creditFieldsContainer;
    @FXML private TextField amountPaidField;
    @FXML private DatePicker dueDatePicker;
    @FXML private Label cartErrorLabel;
    @FXML private Label saleErrorLabel;

    private User currentUser;
    private ObservableList<Medicine> medicineList = FXCollections.observableArrayList();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private ObservableList<User> pharmacistList = FXCollections.observableArrayList();
    private ObservableList<SaleItem> cartItems = FXCollections.observableArrayList();
    private double taxRate = 0.0;

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        if ("PHARMACIST".equals(currentUser.getRole())) {
            pharmacistCombo.setValue(currentUser);
            pharmacistCombo.setDisable(true);
        }
    }

    @FXML
    public void initialize() {
        setupTables();
        loadInitialData();

        // Listener for payment type change
        creditRadio.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            creditFieldsContainer.setVisible(isNowSelected);
            creditFieldsContainer.setManaged(isNowSelected);
        });
    }

    private void setupTables() {
        cartMedNameCol.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        cartQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartPriceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));
        cartTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        cartTable.setItems(cartItems);
    }

    private void loadInitialData() {
        // Load medicines
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM medicines WHERE quantity > 0")) {
            while (rs.next()) {
                medicineList.add(new Medicine(rs.getInt("id"), rs.getString("name"), rs.getString("company"), rs.getString("batch_number"), rs.getInt("quantity"), rs.getDouble("price"), rs.getString("expiry_date"), rs.getBoolean("requires_prescription")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        medicineCombo.setItems(medicineList);

        // Load customers
        customerList.clear();
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {
            while (rs.next()) {
                customerList.add(new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("contact"), rs.getString("address"), rs.getInt("age"), rs.getString("gender"), rs.getDouble("credit_balance")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        customerCombo.setItems(customerList);

        // Load pharmacists
        pharmacistList.clear();
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE role = 'PHARMACIST' OR role = 'ADMIN'")) {
            while (rs.next()) {
                pharmacistList.add(new User(rs.getInt("id"), rs.getString("username"), null, rs.getString("role")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        pharmacistCombo.setItems(pharmacistList);

        // Load tax rate
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT setting_value FROM settings WHERE setting_key = 'tax_rate'")) {
            if (rs.next()) {
                taxRate = Double.parseDouble(rs.getString("setting_value"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void onAddToCart() {
        Medicine selectedMed = medicineCombo.getValue();
        if (selectedMed == null || qtyField.getText().trim().isEmpty()) {
            showCartError("Please select a medicine and enter quantity.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText());
        } catch (NumberFormatException e) {
            showCartError("Quantity must be a valid number.");
            return;
        }

        if (qty <= 0 || qty > selectedMed.getQuantity()) {
            showCartError("Invalid quantity or not enough stock.");
            return;
        }

        cartItems.add(new SaleItem(0, 0, selectedMed.getId(), selectedMed.getName(), qty, selectedMed.getPrice()));
        updateTotals();
        qtyField.clear();
        medicineCombo.getSelectionModel().clearSelection();
        hideCartError();
    }

    private void updateTotals() {
        double subtotal = cartItems.stream().mapToDouble(SaleItem::getTotalPrice).sum();
        double tax = subtotal * taxRate;
        double total = subtotal + tax;

        subtotalLabel.setText(String.format("%.2f ETB", subtotal));
        taxLabel.setText(String.format("%.2f ETB", tax));
        totalLabel.setText(String.format("%.2f ETB", total));
    }

    @FXML
    private void onCompleteSale() {
        if (pharmacistCombo.getValue() == null) {
            showSaleError("You must select a pharmacist to proceed.");
            return;
        }
        if (customerCombo.getValue() == null) {
            showSaleError("You must select a customer before completing the sale.");
            return;
        }
        
        if (cartItems.isEmpty()) {
            showSaleError("Cart is empty.");
            return;
        }

        double subtotal = cartItems.stream().mapToDouble(SaleItem::getTotalPrice).sum();
        double tax = subtotal * taxRate;
        double total = subtotal + tax;
        double amountPaid = total;
        String saleType = "CASH";
        LocalDate dueDate = null;

        if (creditRadio.isSelected()) {
            saleType = "CREDIT";
            try {
                amountPaid = amountPaidField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(amountPaidField.getText());
            } catch (NumberFormatException e) {
                showSaleError("Amount paid must be a valid number.");
                return;
            }
            dueDate = dueDatePicker.getValue();
            if (dueDate == null) {
                showSaleError("Due date is required for credit sales.");
                return;
            }
        }

        Connection conn = null;
        try {
            conn = DBconnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Create Sale Record
            String saleSQL = "INSERT INTO sales (customer_id, total_amount, tax_amount, final_amount, amount_paid, sale_type, due_date, sale_date, pharmacist_username) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement salePstmt = conn.prepareStatement(saleSQL, Statement.RETURN_GENERATED_KEYS);
            salePstmt.setInt(1, customerCombo.getValue().getId());
            salePstmt.setDouble(2, subtotal);
            salePstmt.setDouble(3, tax);
            salePstmt.setDouble(4, total);
            salePstmt.setDouble(5, amountPaid);
            salePstmt.setString(6, saleType);
            salePstmt.setDate(7, dueDate != null ? Date.valueOf(dueDate) : null);
            salePstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            salePstmt.setString(9, pharmacistCombo.getValue().getUsername());
            salePstmt.executeUpdate();

            ResultSet generatedKeys = salePstmt.getGeneratedKeys();
            int saleId;
            if (generatedKeys.next()) {
                saleId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating sale failed, no ID obtained.");
            }

            // 2. Create Sale Items Records & Update Stock
            String itemSQL = "INSERT INTO sale_items (sale_id, medicine_id, quantity, price_per_unit, total_price) VALUES (?, ?, ?, ?, ?)";
            String stockSQL = "UPDATE medicines SET quantity = quantity - ? WHERE id = ?";
            PreparedStatement itemPstmt = conn.prepareStatement(itemSQL);
            PreparedStatement stockPstmt = conn.prepareStatement(stockSQL);

            for (SaleItem item : cartItems) {
                itemPstmt.setInt(1, saleId);
                itemPstmt.setInt(2, item.getMedicineId());
                itemPstmt.setInt(3, item.getQuantity());
                itemPstmt.setDouble(4, item.getPricePerUnit());
                itemPstmt.setDouble(5, item.getTotalPrice());
                itemPstmt.addBatch();

                stockPstmt.setInt(1, item.getQuantity());
                stockPstmt.setInt(2, item.getMedicineId());
                stockPstmt.addBatch();
            }
            itemPstmt.executeBatch();
            stockPstmt.executeBatch();

            // 3. Update Customer Credit Balance if needed
            if ("CREDIT".equals(saleType)) {
                String creditSQL = "UPDATE customers SET credit_balance = credit_balance + ? WHERE id = ?";
                PreparedStatement creditPstmt = conn.prepareStatement(creditSQL);
                creditPstmt.setDouble(1, total - amountPaid);
                creditPstmt.setInt(2, customerCombo.getValue().getId());
                creditPstmt.executeUpdate();
            }

            conn.commit();
            AuditLogger.log(currentUser.getUsername(), AuditLogger.ActionType.SALE_CREATED, "Sale ID: " + saleId + ", Total: " + total);
            
            Sale completedSale = new Sale(saleId, customerCombo.getValue().getName(), subtotal, tax, total, saleType, LocalDateTime.now(), pharmacistCombo.getValue().getUsername());
            completedSale.setAmountPaid(amountPaid);
            showReceipt(completedSale, cartItems);

            resetForm();

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            showSaleError("Sale failed. Transaction rolled back.");
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void showReceipt(Sale sale, ObservableList<SaleItem> items) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("receipt-view.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Sale Receipt");
            stage.setScene(new Scene(loader.load()));

            ReceiptController controller = loader.getController();
            controller.generateReceipt(sale, items);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add-customer-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Customer");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(customerCombo.getScene().getWindow());
            Scene scene = new Scene(loader.load());
            dialogStage.setScene(scene);

            AddCustomerController controller = loader.getController();
            controller.setPosController(this);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCustomerToList(Customer customer) {
        customerList.add(customer);
        customerCombo.setItems(customerList);
        customerCombo.getSelectionModel().select(customer);
    }

    private void resetForm() {
        cartItems.clear();
        updateTotals();
        customerCombo.getSelectionModel().clearSelection();
        if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
            pharmacistCombo.getSelectionModel().clearSelection();
        }
        cashRadio.setSelected(true);
        amountPaidField.clear();
        dueDatePicker.setValue(null);
        hideCartError();
        hideSaleError();
        loadInitialData(); // Refresh stock
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showCartError(String message) {
        cartErrorLabel.setText(message);
        cartErrorLabel.setVisible(true);
        cartErrorLabel.setManaged(true);
    }

    private void hideCartError() {
        cartErrorLabel.setVisible(false);
        cartErrorLabel.setManaged(false);
    }

    private void showSaleError(String message) {
        saleErrorLabel.setText(message);
        saleErrorLabel.setVisible(true);
        saleErrorLabel.setManaged(true);
    }

    private void hideSaleError() {
        saleErrorLabel.setVisible(false);
        saleErrorLabel.setManaged(false);
    }
}
