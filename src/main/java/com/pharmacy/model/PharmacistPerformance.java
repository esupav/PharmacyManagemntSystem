package com.pharmacy.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PharmacistPerformance {
    private final StringProperty pharmacistUsername;
    private final DoubleProperty totalSales;
    private final IntegerProperty numberOfSales;

    public PharmacistPerformance(String pharmacistUsername, double totalSales, int numberOfSales) {
        this.pharmacistUsername = new SimpleStringProperty(pharmacistUsername);
        this.totalSales = new SimpleDoubleProperty(totalSales);
        this.numberOfSales = new SimpleIntegerProperty(numberOfSales);
    }

    // --- Getters for values ---
    public String getPharmacistUsername() { return pharmacistUsername.get(); }
    public double getTotalSales() { return totalSales.get(); }
    public int getNumberOfSales() { return numberOfSales.get(); }

    // --- Property getters for JavaFX ---
    public StringProperty pharmacistUsernameProperty() { return pharmacistUsername; }
    public DoubleProperty totalSalesProperty() { return totalSales; }
    public IntegerProperty numberOfSalesProperty() { return numberOfSales; }
}
