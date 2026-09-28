package com.mobileapp.smartpantrymanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Form screen used both to add a new pantry item and to edit an existing one.
 * The mode is decided by the "pantry_id" Intent extra:
 *   - pantry_id > 0          -> EDIT mode (fields are prefilled)
 *   - pantry_id missing / -1 -> ADD mode (fields are blank)
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Key of the Intent extra that carries the pantry item id. */
    public static final String EXTRA_PANTRY_ID = "pantry_id";

    private DatabaseHelper databaseHelper;

    private EditText editName;
    private EditText editQuantity;
    private Spinner spinnerUnit;
    private EditText editExpiry;

    // Options shown in the Unit spinner (a list so we can add an unknown unit when editing).
    private final List<String> units = new ArrayList<>(
            Arrays.asList("piece", "g", "kg", "ml", "l", "tbsp", "tsp", "cup"));
    private ArrayAdapter<String> unitAdapter;

    private int pantryId = -1;        // -1 means ADD mode
    private boolean isEditMode = false;

    /** Sets up the views, the spinner, the mode (add/edit) and the button listeners. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        // Create the database helper.
        databaseHelper = new DatabaseHelper(this);

        // Find all views from the layout.
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        editExpiry = findViewById(R.id.editExpiry);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        // Fill the spinner with the unit options.
        unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        // Decide between ADD and EDIT mode using the Intent extra (defaults to -1 if missing).
        pantryId = getIntent().getIntExtra(EXTRA_PANTRY_ID, -1);
        isEditMode = pantryId > 0;

        if (isEditMode) {
            setTitle("Edit Ingredient");
            loadExistingItem();
        } else {
            setTitle("Add Ingredient");
        }

        // Save validates the form and writes to the database.
        btnSave.setOnClickListener(v -> saveItem());

        // Cancel just closes the screen without saving.
        btnCancel.setOnClickListener(v -> finish());
    }

    /** Closes the database when the screen is destroyed. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
    }

    /** EDIT mode: loads the item from the database and prefills every field. */
    private void loadExistingItem() {
        PantryItem item = databaseHelper.getPantryItemById(pantryId);

        // Safety check: the item may have been deleted since the list was shown.
        if (item == null) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editName.setText(item.getName());
        editQuantity.setText(String.valueOf(item.getQuantity()));
        editExpiry.setText(item.getExpiryDate());
        selectUnit(item.getUnit());
    }

    /**
     * Selects the given unit in the spinner. If the stored unit is not one of the
     * predefined options (e.g. "pcs" from seed data), it is added so no data is lost.
     */
    private void selectUnit(String unit) {
        if (TextUtils.isEmpty(unit)) {
            return; // keep the default selection
        }
        int index = -1;
        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).equalsIgnoreCase(unit.trim())) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            units.add(unit.trim());
            unitAdapter.notifyDataSetChanged();
            index = units.size() - 1;
        }
        spinnerUnit.setSelection(index);
    }

    /** Validates the form; if valid, builds a PantryItem and saves it (insert or update). */
    private void saveItem() {
        // ---- Read the raw input ----
        String name = editName.getText().toString().trim();
        String quantityText = editQuantity.getText().toString().trim();
        String expiry = editExpiry.getText().toString().trim();
        String unit = (String) spinnerUnit.getSelectedItem();

        // ---- Validate: name must not be empty ----
        if (name.isEmpty()) {
            editName.setError("Name is required");
            editName.requestFocus();
            return;
        }

        // ---- Validate: quantity must be a number greater than 0 ----
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            editQuantity.setError("Enter a valid number");
            editQuantity.requestFocus();
            return;
        }
        if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity <= 0) {
            editQuantity.setError("Quantity must be greater than 0");
            editQuantity.requestFocus();
            return;
        }

        // ---- Validate: expiry is optional, but if present it must be a real YYYY-MM-DD date ----
        if (!expiry.isEmpty() && !isValidDate(expiry)) {
            editExpiry.setError("Use format YYYY-MM-DD");
            editExpiry.requestFocus();
            return;
        }

        // ---- Build the PantryItem ----
        PantryItem item = new PantryItem();
        item.setName(name);
        item.setNormalizedName(DatabaseHelper.normalize(name));
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setExpiryDate(expiry);

        // ---- Save: update in EDIT mode, insert in ADD mode ----
        boolean success;
        if (isEditMode) {
            item.setId(pantryId);
            success = databaseHelper.updatePantryItem(item) > 0;
        } else {
            success = databaseHelper.addPantryItem(item) != -1;
        }

        // ---- Confirm and return to the list ----
        if (success) {
            Toast.makeText(this,
                    isEditMode ? "Item updated" : "Item added",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not save item", Toast.LENGTH_SHORT).show();
        }
    }

    /** Returns true only if the text is a real calendar date in YYYY-MM-DD format. */
    private boolean isValidDate(String text) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false); // rejects things like 2026-13-45
        try {
            format.parse(text);
            return text.length() == 10; // also rejects short forms like 2026-1-5
        } catch (ParseException e) {
            return false;
        }
    }
}