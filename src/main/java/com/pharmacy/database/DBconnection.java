package com.pharmacy.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBconnection {
    private static final String URL = "jdbc:mysql://localhost:3306/pharmacy_db";
    private static final String USER = "root";
    private static final String PASSWORD = "@Etoma1996";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Settings Table (For Tax/VAT)
            stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                    "setting_key VARCHAR(50) PRIMARY KEY," +
                    "setting_value VARCHAR(255) NOT NULL)");
            
            // Insert default tax rate if not exists
            stmt.execute("INSERT IGNORE INTO settings (setting_key, setting_value) VALUES ('tax_rate', '0.15')"); // Default 15%

            // 2. Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) NOT NULL UNIQUE," +
                    "password VARCHAR(255) NOT NULL," +
                    "role VARCHAR(20) NOT NULL)");
            stmt.execute("INSERT IGNORE INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN')");

            // 3. Medicines Table
            stmt.execute("CREATE TABLE IF NOT EXISTS medicines (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "company VARCHAR(100)," +
                    "batch_number VARCHAR(50)," +
                    "quantity INT," +
                    "price DOUBLE," +
                    "expiry_date VARCHAR(20)," +
                    "requires_prescription TINYINT(1) DEFAULT 0)");

            // 4. Customers Table (With Credit Balance)
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "contact VARCHAR(50)," +
                    "address VARCHAR(255)," +
                    "age INT," +
                    "gender VARCHAR(10)," +
                    "credit_balance DOUBLE DEFAULT 0.0)");

            // 5. Sales Table (Enhanced for Credit)
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "customer_id INT," +
                    "total_amount DOUBLE," +
                    "tax_amount DOUBLE," +
                    "final_amount DOUBLE," +
                    "amount_paid DOUBLE," +
                    "sale_type VARCHAR(10)," + // CASH or CREDIT
                    "due_date DATE," +
                    "sale_date DATETIME," +
                    "pharmacist_username VARCHAR(50)," +
                    "FOREIGN KEY(customer_id) REFERENCES customers(id))");

            // 6. Sale Items Table (Details of each sale)
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "sale_id INT," +
                    "medicine_id INT," +
                    "quantity INT," +
                    "price_per_unit DOUBLE," +
                    "total_price DOUBLE," +
                    "FOREIGN KEY(sale_id) REFERENCES sales(id)," +
                    "FOREIGN KEY(medicine_id) REFERENCES medicines(id))");

            // 7. Audit Log Table
            stmt.execute("CREATE TABLE IF NOT EXISTS audit_log (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50)," +
                    "action_type VARCHAR(100)," +
                    "details TEXT," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
