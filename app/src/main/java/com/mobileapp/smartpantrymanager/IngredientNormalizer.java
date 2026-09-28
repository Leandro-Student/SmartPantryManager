package com.mobileapp.smartpantrymanager;

import java.util.Locale;

/**
 * Turns ingredient names into a canonical form so that pantry items and recipe
 * ingredients can be compared reliably.
 *
 * Examples:
 *   "  Tomatoes! " -> "tomato"
 *   "Potatoes"     -> "potato"
 *   "Berries"      -> "berry"
 *   "Eggs"         -> "egg"
 *   "Olive Oil"    -> "olive oil"
 *
 * What matters is that BOTH sides of a comparison go through this same method,
 * so even imperfect singular forms (e.g. "cookies" -> "cooky") still match.
 */
public final class IngredientNormalizer {

    private IngredientNormalizer() {
        // Static utility class - no instances.
    }

    /**
     * Normalizes an ingredient name: lowercase, trimmed, punctuation removed,
     * whitespace collapsed, and the last word converted to its singular form.
     * Returns an empty string for null input.
     */
    public static String normalize(String name) {
        if (name == null) {
            return "";
        }

        // 1) Lowercase and trim surrounding spaces.
        String result = name.trim().toLowerCase(Locale.ROOT);

        // 2) Treat hyphens as spaces ("all-purpose" == "all purpose"), then drop
        //    every other character that is not a letter, digit or whitespace.
        result = result.replace('-', ' ');
        result = result.replaceAll("[^\\p{L}\\p{N}\\s]", "");

        // 3) Collapse repeated whitespace into single spaces.
        result = result.replaceAll("\\s+", " ").trim();
        if (result.isEmpty()) {
            return result;
        }

        // 4) Singularize only the LAST word ("green onions" -> "green onion").
        int lastSpace = result.lastIndexOf(' ');
        String prefix = (lastSpace >= 0) ? result.substring(0, lastSpace + 1) : "";
        String lastWord = result.substring(lastSpace + 1);
        return prefix + singularize(lastWord);
    }

    /** Converts a single plural English word to singular using simple rules. */
    private static String singularize(String word) {
        int len = word.length();

        // Very short words ("gas", "yes") are left alone.
        if (len <= 3) {
            return word;
        }

        // berries -> berry, cherries -> cherry (but "pies" is handled by the plain "s" rule).
        if (word.endsWith("ies") && len > 4) {
            return word.substring(0, len - 3) + "y";
        }

        // tomatoes -> tomato, potatoes -> potato.
        if (word.endsWith("oes") && len > 4) {
            return word.substring(0, len - 2);
        }

        // peaches -> peach, radishes -> radish, boxes -> box, glasses -> glass.
        if (word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("xes") || word.endsWith("sses")) {
            return word.substring(0, len - 2);
        }

        // Words that end in "s" but are not plural: hummus, couscous, asparagus, grass...
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;
        }

        // Default rule: eggs -> egg, olives -> olive.
        if (word.endsWith("s")) {
            return word.substring(0, len - 1);
        }

        return word;
    }
}