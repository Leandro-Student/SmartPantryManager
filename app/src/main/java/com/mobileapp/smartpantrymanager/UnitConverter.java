package com.mobileapp.smartpantrymanager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities into a common base unit so they can be compared:
 *   - weight -> grams
 *   - volume -> milliliters
 *   - pieces -> "count"
 * Units that are not recognised (e.g. "tbsp", "cup", "slice") cannot be converted.
 */
public final class UnitConverter {

    /** The three kinds of measurement we can convert. */
    public static final String TYPE_WEIGHT = "weight";
    public static final String TYPE_VOLUME = "volume";
    public static final String TYPE_COUNT = "count";

    /** Sentinel returned when a unit is unknown or the quantity is invalid. */
    public static final double INVALID = -1;

    // unit name -> measurement type, e.g. "kg" -> "weight"
    private static final Map<String, String> TYPES = new HashMap<>();
    // unit name -> multiplier to reach the base unit, e.g. "kg" -> 1000
    private static final Map<String, Double> FACTORS = new HashMap<>();

    static {
        register(TYPE_WEIGHT, 1, "g", "gram", "grams");
        register(TYPE_WEIGHT, 1000, "kg", "kilogram", "kilograms");
        register(TYPE_VOLUME, 1, "ml", "milliliter", "milliliters", "millilitre", "millilitres");
        register(TYPE_VOLUME, 1000, "l", "liter", "liters", "litre", "litres");
        register(TYPE_COUNT, 1, "piece", "pieces", "pc", "pcs", "clove", "cloves",
                "unit", "units", "count");
    }

    private UnitConverter() {
        // Static utility class - no instances.
    }

    /** Adds one or more unit names to the lookup tables. */
    private static void register(String type, double factor, String... names) {
        for (String name : names) {
            TYPES.put(name, type);
            FACTORS.put(name, factor);
        }
    }

    /** Lowercases, trims and strips dots so "KG", " kg " and "kg." are all the same. */
    public static String normalizeUnit(String unit) {
        if (unit == null) {
            return "";
        }
        return unit.trim().toLowerCase(Locale.ROOT).replace(".", "");
    }

    /**
     * Returns the measurement type of a unit ("weight", "volume" or "count"),
     * or null if the unit is not recognised.
     */
    public static String getUnitType(String unit) {
        return TYPES.get(normalizeUnit(unit));
    }

    /**
     * Converts a quantity into its base unit (grams, milliliters, or count).
     * Examples: toBaseUnit(2, "kg") = 2000, toBaseUnit(1.5, "l") = 1500, toBaseUnit(3, "pcs") = 3.
     *
     * @return the converted quantity, or -1 if the unit is unknown or the quantity is invalid
     */
    public static double toBaseUnit(double quantity, String unit) {
        if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity < 0) {
            return INVALID;
        }
        Double factor = FACTORS.get(normalizeUnit(unit));
        if (factor == null) {
            return INVALID; // unknown unit
        }
        return quantity * factor;
    }
}