package com.mobileapp.smartpantrymanager;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a recipe together with its ingredients.
 * The recipe itself maps to a row in the "recipes" table; the ingredients
 * come from the "recipe_ingredients" table and are attached in memory.
 */
public class Recipe {

    private int id;                              // Database primary key (_id)
    private String name;                         // Recipe title, e.g. "Cheese Omelette"
    private String instructions;                 // Cooking steps as plain text
    private List<RecipeIngredient> ingredients;  // Ingredients required for this recipe

    /** Empty constructor - initialises an empty ingredient list to avoid null checks. */
    public Recipe() {
        this.ingredients = new ArrayList<>();
    }

    /** Constructor without id - used when creating a new recipe before it is saved. */
    public Recipe(String name, String instructions) {
        this.name = name;
        this.instructions = instructions;
        this.ingredients = new ArrayList<>();
    }

    /** Constructor with id - used when reading an existing recipe from the database. */
    public Recipe(int id, String name, String instructions) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.ingredients = new ArrayList<>();
    }

    /** Full constructor including the ingredient list. */
    public Recipe(int id, String name, String instructions, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.ingredients = (ingredients != null) ? ingredients : new ArrayList<>();
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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = (ingredients != null) ? ingredients : new ArrayList<>();
    }

    @Override
    public String toString() {
        return name + " (" + ingredients.size() + " ingredients)";
    }
}