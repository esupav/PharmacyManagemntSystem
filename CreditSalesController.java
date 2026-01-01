package org.example.pharmacymanagementsystem;

import com.pharmacy.database.DBconnection;
import com.pharmacy.model.Sale;
import com.pharmacy.model.User;
import com.pharmacy.util.AuditLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class CreditSalesController implements UserController {

    @FXML private TextField searchCreditSalesField;
    @FXML private TableView<Sale> creditSalesTable;
    @FXML private Label selectedSaleTotalLabel;
    @FXML private TextField paymentAmountField;
    @FXML private Label paymentErrorLabel;

    private User currentUser;
    private ObservableList<Sale> creditSalesList = FXCollections.observableArrayList();
    private FilteredList<Sale> filteredCreditSalesList;
    private Sale selectedSale;

    @Override
    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        setupCreditSalesTable();
        loadCreditSales();

        creditSalesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedSale = newSelection;
            if (selectedSale != null) {
                selectedSaleTotalLabel.setText(String.format("%.2f ETB", selectedSale.getFinalAmount() - selectedSale.getAmountPaid()));
            } else {
                selectedSaleTotalLabel.setText("0.00 ETB");
            }
        });

        searchCreditSalesField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCreditSalesList.setPredicate(sale -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (sale.getCustomerName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(sale.getId()).contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
    }

    private void setupCreditSalesTable() {
        TableColumn<Sale, Integer> idCol = new TableColumn<>("Sale ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Sale, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<Sale, Double> totalCol = new TableColumn<>("Total Amount");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));

        TableColumn<Sale, Double> paidCol = new TableColumn<>("Amount Paid");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));

        TableColumn<Sale, Double> dueCol = new TableColumn<>("Amount Due");
        dueCol.setCellValueFactory(new PropertyValueFactory<>("amountDue"));

        TableColumn<Sale, LocalDateTime> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));

        creditSalesTable.getColumns().addAll(idCol, customerCol, totalCol, paidCol, dueCol, dateCol);
        filteredCreditSalesList = new FilteredList<>(creditSalesList, p -> true);
        creditSalesTable.setItems(filteredCreditSalesList);
    }

    private void loadCreditSales() {
        creditSalesList.clear();
        String sql = "SELECT s.id, c.name as cust_name, s.final_amount, s.amount_paid, s.sale_date " +
                     "FROM sales s JOIN customers c ON s.customer_id = c.id " +
                     "WHERE s.sale_type = 'CREDIT' AND s.final_amount > s.amount_paid";
        
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Sale sale = new Sale(
                    rs.getInt("id"),
                    rs.getString("cust_name"),
                    0, 0, // total and tax not needed for this report
                    rs.getDouble("final_amount"),
                    "CREDIT",
                    rs.getTimestamp("sale_date").toLocalDateTime(),
                    null // pharmacist not needed
                );
                sale.setAmountPaid(rs.getDouble("amount_paid"));
                creditSalesList.add(sale);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void makePayment() {
        if (selectedSale == null) {
            showError("Please select a sale from the table.");
            return;
        }

        double paymentAmount;
        try {
            paymentAmount = Double.parseDouble(paymentAmountField.getText());
        } catch (NumberFormatException e) {
            showError("Please enter a valid payment amount.");
            return;
        }

        if (paymentAmount <= 0) {
            showError("Payment amount must be positive.");
            return;
        }

        double amountDue = selectedSale.getFinalAmount() - selectedSale.getAmountPaid();
        if (paymentAmount > amountDue) {
            showError("Payment cannot be greater than the amount due.");
            return;
        }

        String sql = "UPDATE sales SET amount_paid = amount_paid + ? WHERE id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, paymentAmount);
            pstmt.setInt(2, selectedSale.getId());
            pstmt.executeUpdate();

            AuditLogger.log(currentUser.getUsername(), AuditLogger.ActionType.CREDIT_PAYMENT, "Payment of " + paymentAmount + " for Sale ID: " + selectedSale.getId());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Payment made successfully.");
            
            loadCreditSales(); // Refresh the table
            paymentAmountField.clear();
            hideError();

        } catch (SQLException e) {
            showError("Failed to make payment.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        paymentErrorLabel.setText(message);
        paymentErrorLabel.setVisible(true);
        paymentErrorLabel.setManaged(true);
    }

    private void hideError() {
        paymentErrorLabel.setVisible(false);
        paymentErrorLabel.setManaged(false);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
