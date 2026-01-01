package org.example.pharmacymanagementsystem;

import com.pharmacy.database.DBconnection;
import com.pharmacy.model.Sale;
import com.pharmacy.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DailyReportController implements UserController {

    @FXML private Label totalSalesLabel;
    @FXML private Label numSalesLabel;
    @FXML private TableView<Sale> dailySalesReportTable;

    private User currentUser;

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        loadDailyReport();
    }

    @FXML
    public void initialize() {
        setupDailySalesReportTable();
    }

    private void setupDailySalesReportTable() {
        TableColumn<Sale, Integer> idCol = new TableColumn<>("Sale ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Sale, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<Sale, Double> totalCol = new TableColumn<>("Total Amount");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));

        TableColumn<Sale, LocalDateTime> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));

        dailySalesReportTable.getColumns().addAll(idCol, customerCol, totalCol, dateCol);
    }

    private void loadDailyReport() {
        if (currentUser == null) return;

        ObservableList<Sale> sales = FXCollections.observableArrayList();
        String sql = "SELECT s.id, c.name as cust_name, s.final_amount, s.sale_date " +
                     "FROM sales s JOIN customers c ON s.customer_id = c.id " +
                     "WHERE s.pharmacist_username = ? AND DATE(s.sale_date) = CURDATE()";
        
        double totalSales = 0;
        int numSales = 0;

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, currentUser.getUsername());
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                double finalAmount = rs.getDouble("final_amount");
                sales.add(new Sale(
                    rs.getInt("id"),
                    rs.getString("cust_name"),
                    0, 0, // total and tax not needed for this report
                    finalAmount,
                    null, // sale type not needed
                    rs.getTimestamp("sale_date").toLocalDateTime(),
                    currentUser.getUsername()
                ));
                totalSales += finalAmount;
                numSales++;
            }
            dailySalesReportTable.setItems(sales);
            totalSalesLabel.setText(String.format("%.2f ETB", totalSales));
            numSalesLabel.setText(String.valueOf(numSales));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
