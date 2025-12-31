package com.pharmacy.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Sale {
    private final IntegerProperty id;
    private final StringProperty customerName;
    private final DoubleProperty totalAmount;
    private final DoubleProperty taxAmount;
    private final DoubleProperty finalAmount;
    private final StringProperty saleType;
    private final ObjectProperty<LocalDateTime> saleDate;
    private final StringProperty pharmacistUsername;

    public Sale(int id, String customerName, double totalAmount, double taxAmount, double finalAmount, String saleType, LocalDateTime saleDate, String pharmacistUsername) {
        this.id = new SimpleIntegerProperty(id);
        this.customerName = new SimpleStringProperty(customerName);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
        this.taxAmount = new SimpleDoubleProperty(taxAmount);
        this.finalAmount = new SimpleDoubleProperty(finalAmount);
        this.saleType = new SimpleStringProperty(saleType);
        this.saleDate = new SimpleObjectProperty<>(saleDate);
        this.pharmacistUsername = new SimpleStringProperty(pharmacistUsername);
    }

    // --- Getters for values ---
    public int getId() { return id.get(); }
    public String getCustomerName() { return customerName.get(); }
    public double getTotalAmount() { return totalAmount.get(); }
    public double getTaxAmount() { return taxAmount.get(); }
    public double getFinalAmount() { return finalAmount.get(); }
    public String getSaleType() { return saleType.get(); }
    public LocalDateTime getSaleDate() { return saleDate.get(); }
    public String getPharmacistUsername() { return pharmacistUsername.get(); }

    // --- Property getters for JavaFX ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty customerNameProperty() { return customerName; }
    public DoubleProperty totalAmountProperty() { return totalAmount; }
    public DoubleProperty taxAmountProperty() { return taxAmount; }
    public DoubleProperty finalAmountProperty() { return finalAmount; }
    public StringProperty saleTypeProperty() { return saleType; }
    public ObjectProperty<LocalDateTime> saleDateProperty() { return saleDate; }
    public StringProperty pharmacistUsernameProperty() { return pharmacistUsername; }
}
