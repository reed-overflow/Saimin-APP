package com.reedoverflow.saiminapp.ui.settings;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import com.reedoverflow.saiminapp.R;

public class IntroDialog extends DialogFragment {
    public static final String COMPLETED = "intro_completed";
    private int page;
    private final int[] titles = {R.string.intro_welcome, R.string.intro_modes, R.string.intro_play};
    private final int[] bodies = {R.string.intro_welcome_body, R.string.intro_modes_body, R.string.intro_play_body};

    @NonNull
    @Override public Dialog onCreateDialog(Bundle state) {
        if (state != null) page = Math.max(0, Math.min(2, state.getInt("page")));
        setCancelable(false);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(titles[page]).setMessage(bodies[page])
                .setNegativeButton(R.string.skip, (ignored, which) -> complete())
                .setPositiveButton(page == 2 ? R.string.get_started : R.string.next, null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (page == 2) { complete(); return; }
            page++;
            dialog.setTitle(titles[page]);
            dialog.setMessage(getString(bodies[page]));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(page == 2 ? R.string.get_started : R.string.next);
        }));
        return dialog;
    }

    private void complete() {
        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putBoolean(COMPLETED, true).apply();
        dismiss();
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("page", page);
    }
}
