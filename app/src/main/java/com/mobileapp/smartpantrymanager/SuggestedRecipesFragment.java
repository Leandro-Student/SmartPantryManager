package com.mobileapp.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the recipes the user can make right now with the ingredients in their pantry
 * (strict matching: every ingredient must be present in sufficient quantity).
 */
public class SuggestedRecipesFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private RecipeAdapter adapter;
    private RecyclerView recipesRecyclerView;
    private TextView emptyStateText;

    /** Inflates the layout and sets up the database helper and RecyclerView (runs when the view is created). */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggested_recipes, container, false);

        // Use the application context so the helper never holds on to an Activity.
        databaseHelper = new DatabaseHelper(requireContext().getApplicationContext());

        // Find the views from the layout.
        recipesRecyclerView = view.findViewById(R.id.recipesRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        // Vertical list backed by our adapter (starts empty; filled in onResume).
        adapter = new RecipeAdapter(new ArrayList<>(), this::onRecipeClicked);
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recipesRecyclerView.setAdapter(adapter);

        return view;
    }

    /** Recalculates the suggestions every time the screen is shown, so pantry changes appear immediately. */
    @Override
    public void onResume() {
        super.onResume();
        loadSuggestions();
    }

    /** Closes the database when the fragment's view is destroyed. */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    /**
     * Loads the pantry and all recipes, runs the strict matching, and updates the screen.
     * Shows the empty-state message when nothing matches.
     */
    private void loadSuggestions() {
        List<PantryItem> pantry = databaseHelper.getAllPantryItems();
        List<Recipe> allRecipes = databaseHelper.getAllRecipesWithIngredients();

        List<Recipe> matches = MatchingEngine.getStrictMatches(pantry, allRecipes);
        adapter.setRecipes(matches);

        if (matches.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recipesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recipesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /** Recipe row tapped: open the detail screen, passing the recipe's id. */
    private void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}