package com.mobileapp.smartpantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Decides which recipes can be cooked with what is currently in the pantry.
 *
 * STRICT MATCHING means a recipe is suggested only if EVERY ingredient passes:
 *   1. the ingredient exists in the pantry (names compared after normalization), AND
 *   2. the pantry holds at least the required amount, compared in a common base unit.
 * A single missing ingredient, incompatible unit or shortfall rules the recipe out.
 */
public final class MatchingEngine {

    // Tiny tolerance so floating-point rounding (e.g. 0.1 + 0.2) never causes a false failure.
    private static final double EPSILON = 1e-9;

    private MatchingEngine() {
        // Static utility class - no instances.
    }

    /**
     * Returns the recipes that can be fully made from the given pantry.
     *
     * @param pantry     everything the user currently has
     * @param allRecipes every recipe, each with its ingredient list loaded
     */
    public static List<Recipe> getStrictMatches(List<PantryItem> pantry, List<Recipe> allRecipes) {
        List<Recipe> matches = new ArrayList<>();
        if (pantry == null || allRecipes == null) {
            return matches;
        }

        // STEP 1: Summarise the pantry once, so each ingredient check is a fast lookup.
        Map<String, Double> pantryTotals = buildPantryTotals(pantry);

        // STEP 2: Test every recipe against that summary.
        for (Recipe recipe : allRecipes) {
            if (canMake(recipe, pantryTotals)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    /**
     * Builds a map of "ingredient + unit group" -> total quantity on hand.
     * If the pantry has the same ingredient several times (e.g. two packs of flour),
     * the amounts are added together. Example: 500 g flour + 1 kg flour -> "flour|weight" = 1500.
     */
    private static Map<String, Double> buildPantryTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = makeKey(item.getName(), item.getUnit());
            double amount = comparableQuantity(item.getQuantity(), item.getUnit());
            Double existing = totals.get(key);
            totals.put(key, (existing == null) ? amount : existing + amount);
        }
        return totals;
    }

    /** Returns true only if every ingredient of the recipe is available in sufficient quantity. */
    private static boolean canMake(Recipe recipe, Map<String, Double> pantryTotals) {
        List<RecipeIngredient> required = recipe.getIngredients();

        // A recipe with no ingredients is treated as incomplete data, never as a match.
        if (required == null || required.isEmpty()) {
            return false;
        }

        for (RecipeIngredient ingredient : required) {
            // Same key format as the pantry: normalized name + unit group.
            String key = makeKey(ingredient.getName(), ingredient.getUnit());

            // CHECK 1: Is the ingredient in the pantry (with a compatible unit)?
            // If the name is missing, or it is stored in an incompatible unit
            // (e.g. pantry has flour in grams but the recipe needs it in pieces),
            // the key will not be found and the recipe fails.
            Double available = pantryTotals.get(key);
            if (available == null) {
                return false;
            }

            // CHECK 2: Is there enough? Both amounts are in the same base unit here.
            double needed = comparableQuantity(ingredient.getQuantity(), ingredient.getUnit());
            if (available + EPSILON < needed) {
                return false;
            }
        }

        // Every ingredient passed both checks.
        return true;
    }

    /**
     * Builds the lookup key "normalizedName|unitGroup".
     * - Known units are grouped by type ("weight", "volume", "count"), so grams and kilograms
     *   compare with each other but never with milliliters or pieces.
     * - Unknown units (tbsp, cup, slice...) are grouped by their own name, so they only
     *   match the exact same unit ("tbsp" with "tbsp"). This is a fallback because
     *   UnitConverter cannot convert them.
     */
    private static String makeKey(String name, String unit) {
        String type = UnitConverter.getUnitType(unit);
        String group = (type != null) ? type : "raw:" + UnitConverter.normalizeUnit(unit);
        return IngredientNormalizer.normalize(name) + "|" + group;
    }

    /**
     * Returns the quantity in base units when the unit is known (grams / ml / count),
     * or the raw quantity for unknown units (which are only ever compared to the same unit).
     */
    private static double comparableQuantity(double quantity, String unit) {
        double base = UnitConverter.toBaseUnit(quantity, unit);
        return (base >= 0) ? base : quantity;
    }
}