package com.hospital.model;

public class PrescriptionItem {
    private int id;
    private int prescriptionId;
    private int inventoryId;
    private int quantity;
    private String dosageInstructions;

    // Helper fields for display (requires join in DAO)
    private String medicineName;

    public PrescriptionItem() {
    }

    public PrescriptionItem(int id, int prescriptionId, int inventoryId, int quantity, String dosageInstructions) {
        this.id = id;
        this.prescriptionId = prescriptionId;
        this.inventoryId = inventoryId;
        this.quantity = quantity;
        this.dosageInstructions = dosageInstructions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDosageInstructions() {
        return dosageInstructions;
    }

    public void setDosageInstructions(String dosageInstructions) {
        this.dosageInstructions = dosageInstructions;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }
}
