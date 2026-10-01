package com.reedoverflow.saiminapp.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.reedoverflow.saiminapp.R;

import org.json.JSONException;

import java.math.BigDecimal;

public final class ModeStore {
    private static final String KEY = "custom_modes_v1";
    private final SharedPreferences preferences;
    public final ModeLibrary library;

    public ModeStore(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        ModeLibrary loaded = null;
        String stored = preferences.getString(KEY, null);
        if (stored != null) {
            try {
                loaded = ModeLibrary.fromJson(stored);
            } catch (JSONException ignored) {
                // Retain the original document for recovery until the user next edits a mode.
            }
        }
        library = loaded != null ? loaded : defaults(context);
        if (stored == null) save();
    }

    private ModeLibrary defaults(Context context) {
        ModeLibrary result = new ModeLibrary();
        String[] names = context.getResources().getStringArray(R.array.mode_entries);
        ModeConfig basic = new ModeConfig("legacy_basic", names[0]);
        ModeConfig extended = new ModeConfig("legacy_extended", names[1]);
        extended.parameters.add(new ParameterConfig("legacy_sensitive",
                context.getString(R.string.extend_sensitive), BigDecimal.ZERO,
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(3)));
        extended.parameters.add(new ParameterConfig("legacy_obey",
                context.getString(R.string.extend_obey), BigDecimal.ZERO,
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(5)));
        result.modes.add(basic);
        result.modes.add(extended);
        // Old releases stored this as either an integer or a ListPreference string.
        Object oldSelection = preferences.getAll().get("switch");
        result.selectedId = "0".equals(String.valueOf(oldSelection)) ? basic.id : extended.id;
        return result;
    }

    public void save() {
        try {
            preferences.edit().putString(KEY, library.toJson()).apply();
        } catch (JSONException exception) {
            throw new IllegalStateException("Unable to encode mode settings", exception);
        }
    }
}
