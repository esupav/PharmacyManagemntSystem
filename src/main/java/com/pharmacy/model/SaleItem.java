package com.pharmacy.model;

public class SaleItem {
    private int id;
    private int saleId;
    private int medicineId;
    private String medicineName;
    private int quantity;
    private double pricePerUnit;
    private double totalPrice;

    public SaleItem(int id, int saleId, int medicineId, String medicineName, int quantity, double pricePerUnit) {
        this.id = id;
        this.saleId = saleId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.totalPrice = quantity * pricePerUnit;
    }

    // Getters
    public int getId() { return id; }
    public int getSaleId() { return saleId; }
    public int getMedicineId() { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public int getQuantity() { return quantity; }
    public double getPricePerUnit() { return pricePerUnit; }
    public double getTotalPrice() { return totalPrice; }
}
