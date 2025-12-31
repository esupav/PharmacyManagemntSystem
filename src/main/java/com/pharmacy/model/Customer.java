package com.pharmacy.model;

import javafx.beans.property.*;

public class Customer {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty contact;
    private final StringProperty address;
    private final IntegerProperty age;
    private final StringProperty gender;
    private final DoubleProperty creditBalance;

    public Customer(int id, String name, String contact, String address, int age, String gender, double creditBalance) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.contact = new SimpleStringProperty(contact);
        this.address = new SimpleStringProperty(address);
        this.age = new SimpleIntegerProperty(age);
        this.gender = new SimpleStringProperty(gender);
        this.creditBalance = new SimpleDoubleProperty(creditBalance);
    }

    // --- Getters for values ---
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getContact() { return contact.get(); }
    public String getAddress() { return address.get(); }
    public int getAge() { return age.get(); }
    public String getGender() { return gender.get(); }
    public double getCreditBalance() { return creditBalance.get(); }

    // --- Property getters for JavaFX ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty addressProperty() { return address; }
    public IntegerProperty ageProperty() { return age; }
    public StringProperty genderProperty() { return gender; }
    public DoubleProperty creditBalanceProperty() { return creditBalance; }

    @Override
    public String toString() {
        return getName();
    }
}
