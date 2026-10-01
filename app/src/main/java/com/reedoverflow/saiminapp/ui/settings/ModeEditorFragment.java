package com.reedoverflow.saiminapp.ui.settings;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.reedoverflow.saiminapp.R;
import com.reedoverflow.saiminapp.data.ModeConfig;
import com.reedoverflow.saiminapp.data.ModeStore;
import com.reedoverflow.saiminapp.data.ParameterConfig;

public class ModeEditorFragment extends PreferenceFragmentCompat implements ModeFormDialog.Host {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        refresh();
    }

    @Override
    public void onModeSaved() {
        refresh();
    }

    private void refresh() {
        ModeStore store = new ModeStore(requireContext());
        String id = requireArguments().getString("mode_id");
        ModeConfig mode = store.library.find(id);
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(requireContext());
        setPreferenceScreen(screen);
        if (mode == null) return;

        PreferenceCategory general = category(screen, R.string.edit_mode);
        action(general, "name", R.string.mode_name, mode.name,
                () -> ModeFormDialog.show(this, ModeFormDialog.NAME, id, null));

        PreferenceCategory animation = category(screen, R.string.animation_settings);
        action(animation, "visual_editor", R.string.animation_visual_editor,
                getString(R.string.animation_visual_summary), () -> {
                    if (getChildFragmentManager().findFragmentByTag("animation_editor") == null)
                        AnimationEditorDialog.create(id).show(getChildFragmentManager(), "animation_editor");
                });

        PreferenceCategory parameters = category(screen, R.string.parameters_title);
        for (ParameterConfig parameter : mode.parameters) {
            Preference row = new Preference(requireContext());
            row.setKey("parameter_" + parameter.id);
            row.setTitle(parameter.name);
            row.setSummary(getString(R.string.parameter_summary, ParameterConfig.format(parameter.min),
                    ParameterConfig.format(parameter.max), ParameterConfig.format(parameter.step),
                    ParameterConfig.format(parameter.defaultValue)));
            row.setOnPreferenceClickListener(preference -> {
                ModeFormDialog.show(this, ModeFormDialog.PARAMETER, id, parameter.id);
                return true;
            });
            parameters.addPreference(row);
        }
        action(parameters, "add_parameter", R.string.add_parameter,
                getString(R.string.add_parameter_summary),
                () -> ModeFormDialog.show(this, ModeFormDialog.PARAMETER, id, null));

        PreferenceCategory manage = category(screen, R.string.manage_mode);
        Preference delete = action(manage, "delete_mode", R.string.delete_mode,
                getString(store.library.modes.size() > 1 ? R.string.delete_mode_summary : R.string.keep_one_mode),
                () -> new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.delete_mode)
                        .setMessage(getString(R.string.delete_mode_confirm, mode.name))
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            if (store.library.delete(id)) {
                                store.save();
                                NavHostFragment.findNavController(this).popBackStack();
                            }
                        }).show());
        delete.setEnabled(store.library.modes.size() > 1);
    }

    private PreferenceCategory category(PreferenceScreen screen, int title) {
        PreferenceCategory category = new PreferenceCategory(requireContext());
        category.setTitle(title);
        screen.addPreference(category);
        return category;
    }

    private Preference action(PreferenceCategory category, String key, int title, String summary, Runnable click) {
        Preference preference = new Preference(requireContext());
        preference.setKey(key);
        preference.setPersistent(false);
        preference.setTitle(title);
        preference.setSummary(summary);
        preference.setOnPreferenceClickListener(ignored -> {
            click.run();
            return true;
        });
        category.addPreference(preference);
        return preference;
    }
}

