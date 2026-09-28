package com.mobileapp.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * Shows the full details of one recipe: name, ingredients and instructions.
 * Expects an Intent extra "recipe_id" holding the recipe's database id.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Key of the Intent extra that carries the recipe id. */
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Show a back arrow in the action bar (if the theme provides one).
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView textRecipeName = findViewById(R.id.textRecipeName);
        TextView textIngredients = findViewById(R.id.textIngredients);
        TextView textInstructions = findViewById(R.id.textInstructions);

        // Read the recipe id passed by the previous screen (-1 if missing).
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);

        // Load the recipe (with its ingredients) from the database.
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Recipe recipe = databaseHelper.getRecipeById(recipeId);
        databaseHelper.close();

        // Safety check: bad id or recipe not found.
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show the data and use the recipe name as the screen title.
        setTitle(recipe.getName());
        textRecipeName.setText(recipe.getName());
        textIngredients.setText(formatIngredients(recipe.getIngredients()));
        textInstructions.setText(recipe.getInstructions());
    }

    /** The action bar's back arrow closes this screen. */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Builds one line per ingredient, e.g. "3.0 Eggs", "200.0 g Pasta", "2.0 tbsp Olive Oil".
     * The unit is left out for piece-type units (pcs, piece...) because "3.0 pcs Eggs" reads awkwardly.
     */
    private String formatIngredients(List<RecipeIngredient> ingredients) {
        StringBuilder builder = new StringBuilder();
        for (RecipeIngredient ingredient : ingredients) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(ingredient.getQuantity());

            String unit = ingredient.getUnit();
            boolean isCountUnit = UnitConverter.TYPE_COUNT.equals(UnitConverter.getUnitType(unit));
            if (unit != null && !unit.trim().isEmpty() && !isCountUnit) {
                builder.append(" ").append(unit.trim());
            }
            builder.append(" ").append(ingredient.getName());
        }
        return builder.toString();
    }
}