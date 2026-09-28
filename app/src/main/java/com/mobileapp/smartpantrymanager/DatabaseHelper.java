package com.mobileapp.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite data layer for SmartPantryManager.
 * Manages three tables: pantry_items, recipes and recipe_ingredients.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // ------------------------------------------------------------------
    // Database constants
    // ------------------------------------------------------------------
    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // Table: pantry_items
    private static final String TABLE_PANTRY = "pantry_items";
    private static final String COL_ID = "_id";
    private static final String COL_NAME = "name";
    private static final String COL_NORMALIZED_NAME = "normalized_name";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_UNIT = "unit";
    private static final String COL_EXPIRY_DATE = "expiry_date";

    // Table: recipes
    private static final String TABLE_RECIPES = "recipes";
    private static final String COL_INSTRUCTIONS = "instructions";

    // Table: recipe_ingredients
    private static final String TABLE_INGREDIENTS = "recipe_ingredients";
    private static final String COL_RECIPE_ID = "recipe_id";

    // ------------------------------------------------------------------
    // CREATE TABLE statements
    // ------------------------------------------------------------------
    private static final String CREATE_TABLE_PANTRY =
            "CREATE TABLE " + TABLE_PANTRY + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NAME + " TEXT, "
                    + COL_NORMALIZED_NAME + " TEXT, "
                    + COL_QUANTITY + " REAL, "
                    + COL_UNIT + " TEXT, "
                    + COL_EXPIRY_DATE + " TEXT)";

    private static final String CREATE_TABLE_RECIPES =
            "CREATE TABLE " + TABLE_RECIPES + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NAME + " TEXT, "
                    + COL_INSTRUCTIONS + " TEXT)";

    private static final String CREATE_TABLE_INGREDIENTS =
            "CREATE TABLE " + TABLE_INGREDIENTS + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_RECIPE_ID + " INTEGER, "
                    + COL_NAME + " TEXT, "
                    + COL_NORMALIZED_NAME + " TEXT, "
                    + COL_QUANTITY + " REAL, "
                    + COL_UNIT + " TEXT, "
                    + "FOREIGN KEY(" + COL_RECIPE_ID + ") REFERENCES "
                    + TABLE_RECIPES + "(" + COL_ID + ") ON DELETE CASCADE)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ------------------------------------------------------------------
    // Lifecycle callbacks
    // ------------------------------------------------------------------

    /**
     * Called every time the database is opened. SQLite disables foreign key
     * enforcement by default, so we switch it on here. Without this,
     * "ON DELETE CASCADE" would silently do nothing.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Called the first time the database is created.
     * Creates all tables and then fills the recipe tables with seed data.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PANTRY);
        db.execSQL(CREATE_TABLE_RECIPES);
        db.execSQL(CREATE_TABLE_INGREDIENTS);

        // IMPORTANT: use the 'db' passed in here. Calling getWritableDatabase()
        // from inside onCreate would cause infinite recursion / a crash.
        seedRecipes(db);
    }

    /**
     * Called when DATABASE_VERSION increases. For ,now we simply drop and
     * recreate everything (this will erase user data - replace with proper
     * migrations before release).
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ------------------------------------------------------------------
    // Normalization helper
    // ------------------------------------------------------------------

    /**
     * Normalizes a name for matching: trims surrounding spaces and lowercases.
     * Example: "  Whole MILK " -> "whole milk". Null-safe.
     */
    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    // ------------------------------------------------------------------
    // Seed data
    // ------------------------------------------------------------------

    /** Small factory to keep the seed data below compact: {name, quantity, unit}. */
    private static Object[] ing(String name, double quantity, String unit) {
        return new Object[]{name, quantity, unit};
    }

    /**
     * Inserts a recipe and all of its ingredients using the given database.
     * Each ingredient is an Object[]{String name, Double quantity, String unit}.
     */
    private void insertRecipeWithIngredients(SQLiteDatabase db, String name,
                                             String instructions, Object[]... ingredients) {
        // 1) Insert the recipe row and capture its generated id.
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_NAME, name);
        recipeValues.put(COL_INSTRUCTIONS, instructions);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        // 2) Insert each ingredient linked to that recipe id.
        for (Object[] ingredient : ingredients) {
            String ingName = (String) ingredient[0];
            ContentValues iv = new ContentValues();
            iv.put(COL_RECIPE_ID, recipeId);
            iv.put(COL_NAME, ingName);
            iv.put(COL_NORMALIZED_NAME, normalize(ingName)); // normalized on insert
            iv.put(COL_QUANTITY, (Double) ingredient[1]);
            iv.put(COL_UNIT, (String) ingredient[2]);
            db.insert(TABLE_INGREDIENTS, null, iv);
        }
    }

    /** Seeds 18 simple recipes (2-5 ingredients each). */
    private void seedRecipes(SQLiteDatabase db) {
        insertRecipeWithIngredients(db, "Scrambled Eggs",
                "Melt butter in a pan over medium heat. Whisk eggs with salt, pour in, and stir gently until just set.",
                ing("Eggs", 3, "pcs"), ing("Butter", 1, "tbsp"), ing("Salt", 0.5, "tsp"));

        insertRecipeWithIngredients(db, "Cheese Omelette",
                "Whisk eggs and cook in buttered pan. Add cheese to one half, fold over, and cook until melted.",
                ing("Eggs", 2, "pcs"), ing("Cheese", 50, "g"), ing("Butter", 1, "tbsp"));

        insertRecipeWithIngredients(db, "Buttered Toast",
                "Toast the bread until golden, then spread butter while still warm.",
                ing("Bread", 2, "slice"), ing("Butter", 1, "tbsp"));

        insertRecipeWithIngredients(db, "Grilled Cheese Sandwich",
                "Butter the outside of the bread, put cheese between the slices, and fry on both sides until golden and melted.",
                ing("Bread", 2, "slice"), ing("Cheese", 2, "slice"), ing("Butter", 1, "tbsp"));

        insertRecipeWithIngredients(db, "Plain Pancakes",
                "Mix flour, milk, egg and sugar into a smooth batter. Cook ladlefuls in a lightly oiled pan until bubbles form, then flip.",
                ing("Flour", 1, "cup"), ing("Milk", 1, "cup"), ing("Eggs", 1, "pcs"), ing("Sugar", 1, "tbsp"));

        insertRecipeWithIngredients(db, "Tomato Pasta",
                "Boil pasta until al dente. Saute garlic in olive oil, add chopped tomatoes and simmer 10 minutes. Toss with pasta.",
                ing("Pasta", 200, "g"), ing("Tomato", 3, "pcs"), ing("Garlic", 2, "cloves"), ing("Olive Oil", 2, "tbsp"));

        insertRecipeWithIngredients(db, "Garlic Butter Pasta",
                "Boil pasta until al dente. Melt butter with minced garlic, then toss the pasta through it.",
                ing("Pasta", 200, "g"), ing("Butter", 2, "tbsp"), ing("Garlic", 3, "cloves"));

        insertRecipeWithIngredients(db, "Egg Fried Rice",
                "Saute chopped onion, push aside and scramble the eggs. Add cooked rice and soy sauce, then stir-fry until hot.",
                ing("Rice", 2, "cup"), ing("Eggs", 2, "pcs"), ing("Soy Sauce", 2, "tbsp"), ing("Onion", 1, "pcs"));

        insertRecipeWithIngredients(db, "Banana Milkshake",
                "Blend banana, cold milk and sugar until smooth. Serve immediately.",
                ing("Banana", 2, "pcs"), ing("Milk", 300, "ml"), ing("Sugar", 1, "tbsp"));

        insertRecipeWithIngredients(db, "Simple Guacamole",
                "Mash avocado with lime juice and salt, then fold in diced tomato.",
                ing("Avocado", 2, "pcs"), ing("Lime", 1, "pcs"), ing("Salt", 0.5, "tsp"), ing("Tomato", 1, "pcs"));

        insertRecipeWithIngredients(db, "Caprese Salad",
                "Slice tomatoes and mozzarella, layer with basil leaves, and drizzle with olive oil.",
                ing("Tomato", 2, "pcs"), ing("Mozzarella", 125, "g"), ing("Basil", 6, "leaves"), ing("Olive Oil", 1, "tbsp"));

        insertRecipeWithIngredients(db, "Banana Oatmeal",
                "Simmer oats in milk for 5 minutes, stirring often. Top with sliced banana and honey.",
                ing("Oats", 1, "cup"), ing("Milk", 2, "cup"), ing("Banana", 1, "pcs"), ing("Honey", 1, "tbsp"));

        insertRecipeWithIngredients(db, "Peanut Butter and Jam Sandwich",
                "Spread peanut butter on one slice of bread and jam on the other. Press together and cut in half.",
                ing("Bread", 2, "slice"), ing("Peanut Butter", 2, "tbsp"), ing("Jam", 1, "tbsp"));

        insertRecipeWithIngredients(db, "Mashed Potatoes",
                "Boil peeled potatoes until soft, drain, then mash with butter, warm milk and salt.",
                ing("Potato", 4, "pcs"), ing("Butter", 2, "tbsp"), ing("Milk", 0.25, "cup"), ing("Salt", 0.5, "tsp"));

        insertRecipeWithIngredients(db, "Tuna Salad",
                "Drain the tuna and mix with mayonnaise and finely chopped onion. Serve on bread or crackers.",
                ing("Tuna", 1, "can"), ing("Mayonnaise", 2, "tbsp"), ing("Onion", 0.5, "pcs"));

        insertRecipeWithIngredients(db, "Cheese Quesadilla",
                "Sprinkle cheese over one tortilla, top with a second, and cook in a dry pan until crisp and melted on both sides.",
                ing("Tortilla", 2, "pcs"), ing("Cheese", 100, "g"));

        insertRecipeWithIngredients(db, "French Toast",
                "Whisk eggs, milk and cinnamon. Dip bread slices and fry in a pan until golden on both sides.",
                ing("Bread", 4, "slice"), ing("Eggs", 2, "pcs"), ing("Milk", 0.25, "cup"), ing("Cinnamon", 0.5, "tsp"));

        insertRecipeWithIngredients(db, "Vegetable Soup",
                "Saute chopped onion, add diced carrots and potatoes, pour in broth, and simmer 25 minutes. Season with salt.",
                ing("Carrot", 2, "pcs"), ing("Potato", 2, "pcs"), ing("Onion", 1, "pcs"),
                ing("Vegetable Broth", 1, "l"), ing("Salt", 1, "tsp"));
    }

    // ------------------------------------------------------------------
    // Pantry item CRUD
    // ------------------------------------------------------------------

    /** Converts the current cursor row into a PantryItem, normalizing the name on read. */
    private PantryItem cursorToPantryItem(Cursor c) {
        PantryItem item = new PantryItem();
        item.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        String name = c.getString(c.getColumnIndexOrThrow(COL_NAME));
        item.setName(name);
        item.setNormalizedName(normalize(name));
        item.setQuantity(c.getDouble(c.getColumnIndexOrThrow(COL_QUANTITY)));
        item.setUnit(c.getString(c.getColumnIndexOrThrow(COL_UNIT)));
        item.setExpiryDate(c.getString(c.getColumnIndexOrThrow(COL_EXPIRY_DATE)));
        return item;
    }

    /** Builds ContentValues for a pantry item, normalizing the name for storage. */
    private ContentValues pantryItemToValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName() == null ? null : item.getName().trim());
        values.put(COL_NORMALIZED_NAME, normalize(item.getName()));
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_EXPIRY_DATE, item.getExpiryDate());
        return values;
    }

    /**
     * CREATE: inserts a new pantry item.
     * @return the new row id, or -1 if the insert failed
     */
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_PANTRY, null, pantryItemToValues(item));
    }

    /**
     * READ: returns every pantry item, ordered by expiry date (soonest first)
     * so that items about to expire appear at the top.
     */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null,
                COL_EXPIRY_DATE + " ASC")) {
            while (c.moveToNext()) {
                items.add(cursorToPantryItem(c));
            }
        }
        return items;
    }

    /**
     * READ: returns a single pantry item by id, or null if it does not exist.
     */
    public PantryItem getPantryItemById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_PANTRY, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                return cursorToPantryItem(c);
            }
        }
        return null;
    }

    /**
     * UPDATE: overwrites the pantry item that has the same id as the given object.
     * @return number of rows updated (1 on success, 0 if the id was not found)
     */
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_PANTRY, pantryItemToValues(item), COL_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /**
     * DELETE: removes the pantry item with the given id.
     * @return number of rows deleted (1 on success, 0 if the id was not found)
     */
    public int deletePantryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // ------------------------------------------------------------------
    // Recipe queries
    // ------------------------------------------------------------------

    /** Loads all ingredients belonging to one recipe, normalizing names on read. */
    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, int recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        try (Cursor c = db.query(TABLE_INGREDIENTS, null, COL_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)}, null, null, COL_ID + " ASC")) {
            while (c.moveToNext()) {
                RecipeIngredient ingredient = new RecipeIngredient();
                ingredient.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
                ingredient.setRecipeId(c.getInt(c.getColumnIndexOrThrow(COL_RECIPE_ID)));
                String name = c.getString(c.getColumnIndexOrThrow(COL_NAME));
                ingredient.setName(name);
                ingredient.setNormalizedName(normalize(name));
                ingredient.setQuantity(c.getDouble(c.getColumnIndexOrThrow(COL_QUANTITY)));
                ingredient.setUnit(c.getString(c.getColumnIndexOrThrow(COL_UNIT)));
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    /** Converts the current recipe cursor row into a Recipe and attaches its ingredients. */
    private Recipe cursorToRecipe(SQLiteDatabase db, Cursor c) {
        Recipe recipe = new Recipe();
        recipe.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        recipe.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
        recipe.setInstructions(c.getString(c.getColumnIndexOrThrow(COL_INSTRUCTIONS)));
        recipe.setIngredients(getIngredientsForRecipe(db, recipe.getId()));
        return recipe;
    }

    /**
     * READ: returns every recipe with its ingredient list fully populated,
     * ordered alphabetically by recipe name.
     */
    public List<Recipe> getAllRecipesWithIngredients() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null,
                COL_NAME + " ASC")) {
            while (c.moveToNext()) {
                recipes.add(cursorToRecipe(db, c));
            }
        }
        return recipes;
    }

    /**
     * READ: returns one recipe (with ingredients) by id, or null if not found.
     */
    public Recipe getRecipeById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_RECIPES, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                return cursorToRecipe(db, c);
            }
        }
        return null;
    }
}