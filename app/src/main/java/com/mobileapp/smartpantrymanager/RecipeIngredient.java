package com.mobileapp.smartpantrymanager;

/**
 * Model class representing one ingredient required by a recipe.
 * Maps to a row in the "recipe_ingredients" table.
 */
public class RecipeIngredient {

    private int id;                 // Database primary key (_id)
    private int recipeId;           // Foreign key pointing to the parent recipe
    private String name;            // Display name, e.g. "Eggs"
    private String normalizedName;  // Lowercase/trimmed name used for matching against pantry items
    private double quantity;        // Amount required
    private String unit;            // Unit of measure, e.g. "g", "tbsp", "pcs"

    /** Empty constructor (useful for building the object with setters). */
    public RecipeIngredient() {
    }

    /** Constructor without id - used when creating a new ingredient before it is saved. */
    public RecipeIngredient(int recipeId, String name, String normalizedName,
                            double quantity, String unit) {
        this.recipeId = recipeId;
        this.name = name;
        this.normalizedName = normalizedName;
        this.quantity = quantity;
        this.unit = unit;
    }

    /** Full constructor - used when reading an existing row from the database. */
    public RecipeIngredient(int id, int recipeId, String name, String normalizedName,
                            double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.normalizedName = normalizedName;
        this.quantity = quantity;
        this.unit = unit;
    }

    // ---------------------- Getters and setters ----------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
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

    @Override
    public String toString() {
        return quantity + " " + unit + " " + name;
    }
}