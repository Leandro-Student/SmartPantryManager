package com.mobileapp.smartpantrymanager;

import androidx.annotation.NonNull;

/**
 * Model class representing a single item stored in the user's pantry.
 * Maps to a row in the "pantry_items" table.
 */
public class PantryItem {

    private int id;                 // Database primary key (_id)
    private String name;            // Display name, e.g. "Whole Milk"
    private double quantity;        // Amount on hand
    private String unit;            // Unit of measure, e.g. "g", "ml", "pcs"
    private String expiryDate;      // Expiry date as text, e.g. "2026-12-31"
    private String normalizedName;

    /** Empty constructor (useful for building the object with setters). */
    public PantryItem() {
    }

    // ---------------------- Getters and setters ----------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNormalizedName(String normalizedName) {
        // Lowercase/trimmed name used for matching against recipes
        this.normalizedName = normalizedName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " (" + quantity + " " + unit + ", expires " + expiryDate + ")";
    }
}