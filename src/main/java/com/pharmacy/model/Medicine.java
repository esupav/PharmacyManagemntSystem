package com.pharmacy.model;

import javafx.beans.property.*;

public class Medicine {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty company;
    private final StringProperty batchNumber;
    private final IntegerProperty quantity;
    private final DoubleProperty price;
    private final StringProperty expiryDate;
    private final BooleanProperty requiresPrescription;

    public Medicine(int id, String name, String company, String batchNumber, int quantity, double price, String expiryDate, boolean requiresPrescription) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.company = new SimpleStringProperty(company);
        this.batchNumber = new SimpleStringProperty(batchNumber);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.price = new SimpleDoubleProperty(price);
        this.expiryDate = new SimpleStringProperty(expiryDate);
        this.requiresPrescription = new SimpleBooleanProperty(requiresPrescription);
    }

    // --- Getters for values ---
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getCompany() { return company.get(); }
    public String getBatchNumber() { return batchNumber.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getPrice() { return price.get(); }
    public String getExpiryDate() { return expiryDate.get(); }
    public boolean isRequiresPrescription() { return requiresPrescription.get(); }

    // --- Property getters for JavaFX ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty companyProperty() { return company; }
    public StringProperty batchNumberProperty() { return batchNumber; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty priceProperty() { return price; }
    public StringProperty expiryDateProperty() { return expiryDate; }
    public BooleanProperty requiresPrescriptionProperty() { return requiresPrescription; }
    
    @Override
    public String toString() {
        return getName(); // For display in ComboBox
    }
}
