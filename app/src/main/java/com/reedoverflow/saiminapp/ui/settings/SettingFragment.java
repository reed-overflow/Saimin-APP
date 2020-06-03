package com.reedoverflow.saiminapp.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.reedoverflow.saiminapp.R;

import de.psdev.licensesdialog.LicensesDialog;

// TODO: 2020/6/2 设置界面 
public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case "about_author":
                openLink(getString(R.string.settings_author_detail));
                break;
            case "about_repo":
                openLink(getString(R.string.settings_repo_detail));
                break;
            case "license":
                new LicensesDialog.Builder(getActivity()).setNotices(R.raw.notices).build().show();
                break;
            case "version":
                openLink("https://github.com/reed-overflow/Saimin-APP/releases");
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void openLink(String link) {
        Uri uri = Uri.parse(link);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}