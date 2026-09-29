package com.mobileapp.smartpantrymanager;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter that displays a list of PantryItem objects
 * using the item_pantry.xml row layout.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /**
     * Callback interface so the Activity decides what happens
     * when a row or its delete button is tapped.
     */
    public interface OnPantryItemClickListener {
        void onItemClick(PantryItem item);

        void onDeleteClick(PantryItem item);
    }

    private List<PantryItem> items;
    private final OnPantryItemClickListener listener;

    public PantryAdapter(List<PantryItem> items, OnPantryItemClickListener listener) {
        this.items = (items != null) ? items : new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Replaces the displayed data with a fresh list and refreshes the RecyclerView.
     * Called by the Activity after reloading from the database.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<PantryItem> newItems) {
        this.items = (newItems != null) ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    /** Creates a new row view by inflating item_pantry.xml. */
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    /** Fills an existing row view with the data for the item at the given position. */
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Formats quantity and unit for display, e.g. "2.0 kg".
     * If the unit is missing, only the quantity is shown.
     */
    private static String formatQuantity(double quantity, String unit) {
        String text = String.valueOf(quantity);
        if (unit != null && !unit.trim().isEmpty()) {
            text += " " + unit.trim();
        }
        return text;
    }

    /** Holds references to the views of a single row so they are only looked up once. */
    static class PantryViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textQuantity;
        private final TextView textExpiry;
        private final ImageButton btnDelete;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        /** Binds one PantryItem to the row's views and wires up click listeners. */
        @SuppressLint("SetTextI18n")
        void bind(final PantryItem item, final OnPantryItemClickListener listener) {
            // Show the item's data.
            textName.setText(item.getName());
            textQuantity.setText(formatQuantity(item.getQuantity(), item.getUnit()));

            String expiry = item.getExpiryDate();
            if (expiry == null || expiry.trim().isEmpty()) {
                textExpiry.setText("No expiry date");
            } else {
                textExpiry.setText("Expires: " + expiry);
            }

            // Tapping the whole row notifies the listener.
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });

            // Tapping the delete icon notifies the listener.
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(item);
                }
            });
        }
    }
}