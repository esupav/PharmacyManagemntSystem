package org.example.pharmacymanagementsystem;

import com.pharmacy.database.DBconnection;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Sale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportsController {

    @FXML private DatePicker salesFromDate;
    @FXML private DatePicker salesToDate;
    @FXML private TableView<Sale> salesReportTable;
    @FXML private TableView<Medicine> inventoryReportTable;

    @FXML
    public void initialize() {
        // Initialize tables with default columns
        setupSalesReportTable();
        setupInventoryReportTable();
    }

    private void setupSalesReportTable() {
        TableColumn<Sale, Integer> idCol = new TableColumn<>("Sale ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Sale, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<Sale, Double> totalCol = new TableColumn<>("Total Amount");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));

        TableColumn<Sale, LocalDateTime> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));

        TableColumn<Sale, String> pharmacistCol = new TableColumn<>("Pharmacist");
        pharmacistCol.setCellValueFactory(new PropertyValueFactory<>("pharmacistUsername"));

        salesReportTable.getColumns().addAll(idCol, customerCol, totalCol, dateCol, pharmacistCol);
    }

    private void setupInventoryReportTable() {
        TableColumn<Medicine, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Medicine, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Medicine, String> expiryCol = new TableColumn<>("Expiry Date");
        expiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        inventoryReportTable.getColumns().addAll(nameCol, qtyCol, expiryCol);
    }

    @FXML
    private void generateSalesReport() {
        LocalDate from = salesFromDate.getValue();
        LocalDate to = salesToDate.getValue();

        if (from == null || to == null) {
            // Handle error: show message to user
            return;
        }

        ObservableList<Sale> sales = FXCollections.observableArrayList();
        String sql = "SELECT s.id, c.name as cust_name, s.final_amount, s.sale_date, s.pharmacist_username " +
                     "FROM sales s JOIN customers c ON s.customer_id = c.id " +
                     "WHERE s.sale_date BETWEEN ? AND ?";
        
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setObject(1, from.atStartOfDay());
            pstmt.setObject(2, to.plusDays(1).atStartOfDay());
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sales.add(new Sale(
                    rs.getInt("id"),
                    rs.getString("cust_name"),
                    0, 0, // total and tax not needed for this report
                    rs.getDouble("final_amount"),
                    null, // sale type not needed
                    rs.getTimestamp("sale_date").toLocalDateTime(),
                    rs.getString("pharmacist_username")
                ));
            }
            salesReportTable.setItems(sales);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void generateInventoryReport() {
        ObservableList<Medicine> medicines = FXCollections.observableArrayList();
        String sql = "SELECT name, quantity, expiry_date FROM medicines ORDER BY quantity ASC";
        
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                medicines.add(new Medicine(
                    0, // id not needed
                    rs.getString("name"),
                    null, null, // company and batch not needed
                    rs.getInt("quantity"),
                    0, // price not needed
                    rs.getString("expiry_date"),
                    false // prescription status not needed
                ));
            }
            inventoryReportTable.setItems(medicines);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
