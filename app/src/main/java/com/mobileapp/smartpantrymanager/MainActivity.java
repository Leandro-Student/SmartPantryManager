package com.mobileapp.smartpantrymanager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Hosts the three main screens (Pantry, Recipes, Settings) as fragments
 * and switches between them using the bottom navigation bar.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Swap the fragment whenever a different tab is selected.
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                loadFragment(new PantryListFragment());
                return true;
            } else if (id == R.id.nav_recipes) {
                loadFragment(new SuggestedRecipesFragment());
                return true;
            } else if (id == R.id.nav_settings) {
                loadFragment(new SettingsFragment());
                return true;
            }
            return false;
        });

        // Tapping the tab that is already showing does nothing, so the current
        // fragment is not needlessly recreated (which would reset its state).
        bottomNavigationView.setOnItemReselectedListener(item -> { });

        // Default screen: Suggested Recipes. Selecting the tab triggers the listener above,
        // which loads the fragment and highlights the tab. This only runs on a fresh start;
        // after a rotation Android restores the fragment and the selected tab by itself.
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_recipes);
        }
    }

    /** Replaces whatever is in fragmentContainer with the given fragment. */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}