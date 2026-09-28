package com.mobileapp.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter that shows recipe names using the item_recipe.xml row layout.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    /** Callback so the hosting screen decides what happens when a recipe is tapped. */
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = (recipes != null) ? recipes : new ArrayList<>();
        this.listener = listener;
    }

    /** Replaces the displayed recipes with a new list and refreshes the RecyclerView. */
    public void setRecipes(List<Recipe> newRecipes) {
        this.recipes = (newRecipes != null) ? newRecipes : new ArrayList<>();
        notifyDataSetChanged();
    }

    /** Creates a new row view by inflating item_recipe.xml. */
    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    /** Binds the recipe at the given position to a row. */
    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        holder.bind(recipes.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    /** Holds the row's views so they are only looked up once. */
    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final TextView textRecipeName;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textRecipeName = itemView.findViewById(R.id.textRecipeName);
        }

        /** Shows the recipe name and forwards row taps to the listener. */
        void bind(final Recipe recipe, final OnRecipeClickListener listener) {
            textRecipeName.setText(recipe.getName());
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
            });
        }
    }
}