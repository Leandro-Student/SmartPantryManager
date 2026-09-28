package com.mobileapp.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows every item in the user's pantry (formerly PantryListActivity).
 * The list is reloaded from the database each time the screen is shown.
 */
public class PantryListFragment extends Fragment
        implements PantryAdapter.OnPantryItemClickListener {

    private DatabaseHelper databaseHelper;
    private PantryAdapter adapter;
    private RecyclerView pantryRecyclerView;
    private TextView emptyStateText;

    /** Inflates the layout and sets up the database, RecyclerView and add button. */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pantry_list, container, false);

        // Application context so the helper never holds on to an Activity.
        databaseHelper = new DatabaseHelper(requireContext().getApplicationContext());

        // Find the views from the layout.
        pantryRecyclerView = view.findViewById(R.id.pantryRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        FloatingActionButton fabAddItem = view.findViewById(R.id.fabAddItem);

        // Vertical list backed by our adapter (starts empty; filled in onResume).
        adapter = new PantryAdapter(new ArrayList<>(), this);
        pantryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        pantryRecyclerView.setAdapter(adapter);

        // The FAB opens the Add/Edit screen in ADD mode (no "pantry_id" extra).
        fabAddItem.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditIngredientActivity.class)));

        return view;
    }

    /** Reloads the list every time the screen is shown, e.g. after returning from Add/Edit. */
    @Override
    public void onResume() {
        super.onResume();
        loadPantryItems();
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

    /** Row tapped: open the Add/Edit screen in EDIT mode for that item. */
    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_PANTRY_ID, item.getId());
        startActivity(intent);
    }

    /** Delete button tapped: ask for confirmation before removing the item. */
    @Override
    public void onDeleteClick(final PantryItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete item")
                .setMessage("Are you sure you want to delete \"" + item.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Confirmed: remove from the database, then refresh the list.
                    databaseHelper.deletePantryItem(item.getId());
                    loadPantryItems();
                })
                .setNegativeButton("Cancel", null) // Cancel just closes the dialog.
                .show();
    }
}