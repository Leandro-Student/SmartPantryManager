package com.mobileapp.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen that shows every item in the user's pantry.
 * The list is reloaded from the database each time the screen is shown.
 */
public class PantryListActivity extends AppCompatActivity implements PantryAdapter.OnPantryItemClickListener {

    private DatabaseHelper databaseHelper;
    private PantryAdapter adapter;
    private RecyclerView pantryRecyclerView;
    private TextView emptyStateText;

    /** Sets up the layout, database, RecyclerView and the add button (runs once). */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        // Create the database helper used for all pantry queries.
        databaseHelper = new DatabaseHelper(this);

        // Find the views from the layout.
        pantryRecyclerView = findViewById(R.id.pantryRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        FloatingActionButton fabAddItem = findViewById(R.id.fabAddItem);

        // Configure the RecyclerView: vertical list + our adapter (starts empty).
        adapter = new PantryAdapter(new ArrayList<>(), this);
        pantryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pantryRecyclerView.setAdapter(adapter);

        // Open the Add/Edit screen in "Add" mode when the + button is clicked.
        fabAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
            intent.putExtra("pantry_id", -1); // -1 means Add mode
            startActivity(intent);
        });
    }

    /**
     * Runs every time the screen becomes visible, including when returning
     * from another Activity, so the list always reflects the latest data.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    /** Closes the database when the screen is destroyed. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
    }

    /**
     * Reads all pantry items from the database, gives them to the adapter,
     * and toggles between the list and the empty-state message.
     */
    private void loadPantryItems() {
        List<PantryItem> items = databaseHelper.getAllPantryItems();
        adapter.setItems(items);

        if (items.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            pantryRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            pantryRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /** Called when a row is tapped. Opens the Edit screen with the item's data. */
    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
        intent.putExtra("pantry_id", item.getId()); // Pass the ID to Edit mode
        startActivity(intent);
    }

    /** Called when a row's delete button is tapped. Asks for confirmation first. */
    @Override
    public void onDeleteClick(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete item")
                .setMessage("Are you sure you want to delete \"" + item.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // User confirmed: remove from the database, then refresh the list.
                    databaseHelper.deletePantryItem(item.getId());
                    loadPantryItems();
                })
                .setNegativeButton("Cancel", null) // Cancel just closes the dialog.
                .show();
    }
}