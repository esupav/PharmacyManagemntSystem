package com.pharmacy.model;

public class Supplier {
    private int id;
    private String name;
    private String contact;
    private String address;
    private String licenseNumber;

    public Supplier(int id, String name, String contact, String address, String licenseNumber) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.address = address;
        this.licenseNumber = licenseNumber;
    }

    public Supplier(String name, String contact, String address, String licenseNumber) {
        this.name = name;
        this.contact = contact;
        this.address = address;
        this.licenseNumber = licenseNumber;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
}
