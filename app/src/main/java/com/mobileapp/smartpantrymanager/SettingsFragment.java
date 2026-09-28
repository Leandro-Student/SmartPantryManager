package com.mobileapp.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

/**
 * Simple settings screen. Each switch's state is stored in SharedPreferences.
 */
public class SettingsFragment extends Fragment {

    // Preference file name and keys (public so other classes can read the settings later).
    public static final String PREFS_NAME = "smart_pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "pref_expiry_alerts";
    public static final String KEY_METRIC_UNITS = "pref_metric_units";

    /** Inflates the layout, restores the saved switch states, and starts saving changes. */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        final SharedPreferences prefs =
                requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SwitchCompat switchExpiryAlerts = view.findViewById(R.id.switchExpiryAlerts);
        SwitchCompat switchMetric = view.findViewById(R.id.switchMetric);

        // 1) Load the saved state FIRST (both default to on the first time the app runs)...
        switchExpiryAlerts.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        switchMetric.setChecked(prefs.getBoolean(KEY_METRIC_UNITS, true));

        // 2) ...then attach the listeners, so loading does not trigger a needless save.
        switchExpiryAlerts.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply());

        switchMetric.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_METRIC_UNITS, isChecked).apply());

        return view;
    }
}