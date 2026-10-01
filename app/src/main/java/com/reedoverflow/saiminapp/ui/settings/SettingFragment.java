package com.reedoverflow.saiminapp.ui.settings;

import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceCategory;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.reedoverflow.saiminapp.R;
import com.reedoverflow.saiminapp.MainActivity;
import com.reedoverflow.saiminapp.QuickEntry;
import com.reedoverflow.saiminapp.BuildConfig;
import com.reedoverflow.saiminapp.data.ModeConfig;
import com.reedoverflow.saiminapp.data.ModeStore;

import de.psdev.licensesdialog.LicensesDialog;

public class SettingFragment extends PreferenceFragmentCompat implements ModeFormDialog.Host, SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        Preference version = findPreference("version");
        if (version != null) version.setSummary(BuildConfig.VERSION_NAME);
        SwitchPreferenceCompat notification = findPreference(QuickEntry.KEY);
        if (notification != null) notification.setOnPreferenceChangeListener((preference, value) -> {
            if ((Boolean) value) ((MainActivity) requireActivity()).enableNotificationEntry();
            else QuickEntry.setEnabled(requireContext(), false);
            refreshNotification();
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshModes();
        PreferenceManager.getDefaultSharedPreferences(requireContext()).registerOnSharedPreferenceChangeListener(this);
        refreshNotification();
    }

    @Override public void onPause() {
        PreferenceManager.getDefaultSharedPreferences(requireContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (QuickEntry.KEY.equals(key)) refreshNotification();
    }

    private void refreshNotification() {
        SwitchPreferenceCompat notification = findPreference(QuickEntry.KEY);
        if (notification == null) return;
        boolean enabled = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(QuickEntry.KEY, false);
        notification.setChecked(enabled);
        notification.setSummary(enabled && !QuickEntry.allowed(requireContext())
                ? R.string.notification_permission : R.string.notification_entry_summary);
    }

    @Override
    public void onModeSaved() {
        refreshModes();
    }

    private void refreshModes() {
        ModeStore store = new ModeStore(requireContext());
        ListPreference current = findPreference("current_mode");
        PreferenceCategory list = findPreference("mode_list");
        if (current == null || list == null) return;
        CharSequence[] names = new CharSequence[store.library.modes.size()];
        CharSequence[] ids = new CharSequence[names.length];
        list.removeAll();
        for (int i = 0; i < names.length; i++) {
            ModeConfig mode = store.library.modes.get(i);
            names[i] = mode.name;
            ids[i] = mode.id;
            Preference row = new Preference(requireContext());
            row.setKey("mode_" + mode.id);
            row.setTitle(mode.name);
            row.setSummary(getString(R.string.mode_summary, mode.parameters.size(),
                    getString(mode.fullscreen ? R.string.fullscreen_playback : R.string.inline_playback)));
            row.setOnPreferenceClickListener(preference -> {
                Bundle args = new Bundle();
                args.putString("mode_id", mode.id);
                NavHostFragment.findNavController(this).navigate(R.id.nav_mode_editor, args);
                return true;
            });
            list.addPreference(row);
        }
        current.setEntries(names);
        current.setEntryValues(ids);
        current.setValue(store.library.selected().id);
        current.setSummary(store.library.selected().name);
        current.setOnPreferenceChangeListener((preference, value) -> {
            store.library.selectedId = value.toString();
            store.save();
            current.setSummary(store.library.selected().name);
            return true;
        });
        Preference add = findPreference("add_mode");
        if (add != null) add.setOnPreferenceClickListener(preference -> {
            ModeFormDialog.show(this, ModeFormDialog.NAME, null, null);
            return true;
        });
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (key == null) return super.onPreferenceTreeClick(preference);
        switch (key) {
            case "notification_settings":
                ((MainActivity) requireActivity()).openNotificationSettings();
                return true;
            case "intro":
                if (requireActivity().getSupportFragmentManager().findFragmentByTag("intro") == null)
                    new IntroDialog().show(requireActivity().getSupportFragmentManager(), "intro");
                return true;
            case "check_updates":
            case "version":
                if (getChildFragmentManager().findFragmentByTag("update_check") == null)
                    new UpdateCheckDialog().show(getChildFragmentManager(), "update_check");
                return true;
            case "about_author":
                openLink(getString(R.string.settings_author_detail));
                break;
            case "about_repo":
                openLink(getString(R.string.settings_repo_detail));
                break;
            case "license":
                new LicensesDialog.Builder(getActivity()).setNotices(R.raw.notices).build().show();
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void openLink(String link) {
        Uri uri = Uri.parse(link);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try { startActivity(intent); }
        catch (ActivityNotFoundException exception) {
            Toast.makeText(requireContext(), R.string.no_browser, Toast.LENGTH_LONG).show();
        }
    }
}
