package com.reedoverflow.saiminapp.ui.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.reedoverflow.saiminapp.R;
import com.reedoverflow.saiminapp.data.ModeConfig;
import com.reedoverflow.saiminapp.data.ModeStore;
import com.reedoverflow.saiminapp.data.ParameterConfig;

import java.math.BigDecimal;
import java.util.Locale;

/** DialogFragment retains unsaved input across rotation and process recreation. */
public class ModeFormDialog extends DialogFragment {
    public static final String NAME = "name", PARAMETER = "parameter", SIZE = "size",
            HEIGHT = "height", DURATION = "duration", COLOR = "color", BACKGROUND = "background";
    private EditText[] fields;

    public interface Host {
        void onModeSaved();
    }

    public static void updateHost(Fragment fragment) {
        if (fragment instanceof Host) ((Host) fragment).onModeSaved();
    }

    public static void show(Fragment parent, String kind, String modeId, String parameterId) {
        if (parent.getChildFragmentManager().findFragmentByTag("mode_form") != null) return;
        ModeFormDialog dialog = new ModeFormDialog();
        Bundle args = new Bundle();
        args.putString("kind", kind);
        args.putString("mode_id", modeId);
        args.putString("parameter_id", parameterId);
        dialog.setArguments(args);
        dialog.show(parent.getChildFragmentManager(), "mode_form");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String kind = requireArguments().getString("kind", NAME);
        ModeConfig mode = new ModeStore(requireContext()).library.find(requireArguments().getString("mode_id"));
        ParameterConfig parameter = findParameter(mode);
        int[] labels;
        String[] values;
        int title;
        String help = null;
        if (PARAMETER.equals(kind)) {
            title = parameter == null ? R.string.add_parameter : R.string.edit_parameter;
            labels = new int[]{R.string.parameter_name, R.string.parameter_min, R.string.parameter_max,
                    R.string.parameter_step, R.string.parameter_default};
            values = parameter == null ? new String[]{"", "0", "100", "1", "50"}
                    : new String[]{parameter.name, ParameterConfig.format(parameter.min),
                    ParameterConfig.format(parameter.max), ParameterConfig.format(parameter.step),
                    ParameterConfig.format(parameter.defaultValue)};
            help = getString(R.string.parameter_help);
        } else {
            switch (kind) {
                case SIZE:
                    title = R.string.animation_size;
                    values = new String[]{String.valueOf(mode == null ? 90 : mode.animationSizePercent)};
                    help = getString(R.string.integer_range, 10, 100);
                    break;
                case HEIGHT:
                    title = R.string.animation_height;
                    values = new String[]{String.valueOf(mode == null ? 280 : mode.animationHeightDp)};
                    help = getString(R.string.integer_range, 120, 800);
                    break;
                case DURATION:
                    title = R.string.animation_duration;
                    values = new String[]{String.valueOf(mode == null ? 5000 : mode.animationDurationMs)};
                    help = getString(R.string.integer_range, 1000, 20000);
                    break;
                case COLOR:
                    title = R.string.animation_color;
                    values = new String[]{mode == null ? "#F06292" : mode.animationColor};
                    help = getString(R.string.color_help);
                    break;
                case BACKGROUND:
                    title = R.string.background_color;
                    values = new String[]{mode == null ? "#FFF0F5" : mode.backgroundColor};
                    help = getString(R.string.color_help);
                    break;
                default:
                    title = mode == null ? R.string.add_mode : R.string.mode_name;
                    values = new String[]{mode == null ? "" : mode.name};
                    break;
            }
            labels = new int[]{title};
        }
        if (savedInstanceState != null) {
            String[] saved = savedInstanceState.getStringArray("input");
            if (saved != null && saved.length == values.length) values = saved;
        }
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(padding, padding / 2, padding, padding / 2);
        if (help != null) {
            TextView note = new TextView(requireContext());
            note.setText(help);
            note.setPadding(0, 0, 0, padding / 2);
            content.addView(note);
        }
        fields = new EditText[labels.length];
        for (int i = 0; i < fields.length; i++) {
            TextView label = new TextView(requireContext());
            label.setText(labels[i]);
            content.addView(label);
            EditText field = new AppCompatEditText(requireContext());
            field.setSingleLine(true);
            field.setSaveEnabled(false);
            field.setContentDescription(getString(labels[i]));
            boolean numeric = PARAMETER.equals(kind) ? i > 0
                    : SIZE.equals(kind) || HEIGHT.equals(kind) || DURATION.equals(kind);
            field.setInputType(numeric ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED
                    | InputType.TYPE_NUMBER_FLAG_DECIMAL : InputType.TYPE_CLASS_TEXT);
            field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(numeric ? 30 : 100)});
            field.setText(values[i]);
            content.addView(field, new LinearLayout.LayoutParams(-1, -2));
            fields[i] = field;
        }
        ScrollView scroll = new ScrollView(requireContext());
        scroll.addView(content);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle(title)
                .setView(scroll).setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.save, null);
        if (parameter != null) builder.setNeutralButton(R.string.delete, null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> save(kind));
            if (parameter != null) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view ->
                        new AlertDialog.Builder(requireContext()).setTitle(R.string.delete_parameter)
                                .setMessage(getString(R.string.delete_parameter_confirm, parameter.name))
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(R.string.delete, (confirmation, which) -> {
                                    ModeStore store = new ModeStore(requireContext());
                                    ModeConfig fresh = store.library.find(mode.id);
                                    if (fresh != null) {
                                        fresh.parameters.remove(findParameter(fresh));
                                        store.library.selectedId = store.library.selected().id;
                                        store.save();
                                    }
                                    finish();
                                }).show());
            }
        });
        return dialog;
    }

    private ParameterConfig findParameter(ModeConfig mode) {
        if (mode == null) return null;
        String id = requireArguments().getString("parameter_id");
        for (ParameterConfig parameter : mode.parameters) {
            if (parameter.id.equals(id)) return parameter;
        }
        return null;
    }

    private String input(int index) {
        return fields[index].getText().toString().trim();
    }

    private void save(String kind) {
        ModeStore store = new ModeStore(requireContext());
        String id = requireArguments().getString("mode_id");
        ModeConfig mode = store.library.find(id);
        if (id != null && mode == null) {
            dismiss();
            return;
        }
        if (NAME.equals(kind)) {
            if (input(0).isEmpty()) {
                fields[0].setError(getString(R.string.name_required));
                return;
            }
            if (mode == null) {
                mode = new ModeConfig(input(0));
                store.library.modes.add(mode);
            } else {
                mode.name = input(0);
            }
        } else if (PARAMETER.equals(kind) && mode != null) {
            if (input(0).isEmpty()) {
                fields[0].setError(getString(R.string.name_required));
                return;
            }
            BigDecimal[] numbers = new BigDecimal[4];
            for (int i = 1; i < fields.length; i++) {
                try {
                    if (!input(i).matches("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)")) {
                        throw (new NumberFormatException());
                    }
                    numbers[i - 1] = new BigDecimal(input(i));
                } catch (NumberFormatException exception) {
                    fields[i].setError(getString(R.string.number_required));
                    return;
                }
            }
            try {
                ParameterConfig existing = findParameter(mode);
                ParameterConfig updated = new ParameterConfig(existing == null ? null : existing.id,
                        input(0), numbers[0], numbers[1], numbers[2], numbers[3]);
                if (existing == null) mode.parameters.add(updated);
                else mode.parameters.set(mode.parameters.indexOf(existing), updated);
            } catch (IllegalArgumentException | ArithmeticException exception) {
                fields[4].setError(getString(R.string.parameter_invalid));
                return;
            }
        } else if ((COLOR.equals(kind) || BACKGROUND.equals(kind)) && mode != null) {
            if (!input(0).matches("#[0-9A-Fa-f]{6}")) {
                fields[0].setError(getString(R.string.color_help));
                return;
            }
            if (BACKGROUND.equals(kind)) mode.backgroundColor = input(0).toUpperCase(Locale.ROOT);
            else mode.animationColor = input(0).toUpperCase(Locale.ROOT);
        } else if (mode != null) {
            int min = SIZE.equals(kind) ? 10 : HEIGHT.equals(kind) ? 120 : 1000;
            int max = SIZE.equals(kind) ? 100 : HEIGHT.equals(kind) ? 800 : 20000;
            try {
                int value = Integer.parseInt(input(0));
                if (value < min || value > max) throw new NumberFormatException();
                if (SIZE.equals(kind)) mode.animationSizePercent = value;
                else if (HEIGHT.equals(kind)) mode.animationHeightDp = value;
                else mode.animationDurationMs = value;
            } catch (NumberFormatException exception) {
                fields[0].setError(getString(R.string.integer_range, min, max));
                return;
            }
        }
        store.save();
        finish();
    }

    private void finish() {
        updateHost(getParentFragment());
        dismiss();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (fields != null) {
            String[] values = new String[fields.length];
            for (int i = 0; i < fields.length; i++) values[i] = input(i);
            outState.putStringArray("input", values);
        }
    }
}
