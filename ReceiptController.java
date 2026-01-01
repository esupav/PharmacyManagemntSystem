package org.example.pharmacymanagementsystem;

import com.pharmacy.model.Sale;
import com.pharmacy.model.SaleItem;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.time.format.DateTimeFormatter;

public class ReceiptController {

    @FXML
    private TextArea receiptArea;

    public void generateReceipt(Sale sale, ObservableList<SaleItem> items) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        sb.append("========================================\n");
        sb.append("           PHARMACY RECEIPT\n");
        sb.append("========================================\n\n");

        sb.append(String.format("Receipt ID: %d\n", sale.getId()));
        sb.append(String.format("Date: %s\n", sale.getSaleDate().format(formatter)));
        sb.append(String.format("Pharmacist: %s\n", sale.getPharmacistUsername()));
        sb.append(String.format("Customer: %s\n\n", sale.getCustomerName()));

        sb.append("----------------------------------------\n");
        sb.append(String.format("%-20s %5s %10s\n", "Item", "Qty", "Total"));
        sb.append("----------------------------------------\n");

        for (SaleItem item : items) {
            sb.append(String.format("%-20s %5d %10.2f\n", item.getMedicineName(), item.getQuantity(), item.getTotalPrice()));
        }

        sb.append("\n----------------------------------------\n");
        sb.append(String.format("%26s %10.2f\n", "Subtotal:", sale.getTotalAmount()));
        sb.append(String.format("%26s %10.2f\n", "Tax:", sale.getTaxAmount()));
        sb.append(String.format("%26s %10.2f\n", "TOTAL:", sale.getFinalAmount()));
        sb.append("----------------------------------------\n\n");

        if ("CREDIT".equals(sale.getSaleType())) {
            sb.append(String.format("Amount Paid: %.2f\n", sale.getAmountPaid()));
            sb.append(String.format("Amount Due: %.2f\n", sale.getFinalAmount() - sale.getAmountPaid()));
        }

        sb.append("\nThank you for your business!\n");
        sb.append("========================================\n");

        receiptArea.setText(sb.toString());
    }

    @FXML
    private void onPrint() {
        // This is a placeholder for actual printing logic.
        // In a real application, you would use JavaFX's printing APIs.
        System.out.println("--- Printing Receipt ---");
        System.out.println(receiptArea.getText());
        System.out.println("------------------------");
    }
}
