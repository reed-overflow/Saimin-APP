package com.reedoverflow.saiminapp.ui.settings;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import com.reedoverflow.saiminapp.R;
import com.reedoverflow.saiminapp.data.ModeConfig;
import com.reedoverflow.saiminapp.data.ModeStore;
import com.reedoverflow.saiminapp.ui.home.HypnosisAnimationView;

/** Edits a draft, so Cancel never changes the saved mode. Uses the home renderer. */
public class AnimationEditorDialog extends DialogFragment {
    private ModeConfig draft;
    private HypnosisAnimationView preview;
    private FrameLayout previewFrame;
    private EditText foreground, background;

    public static AnimationEditorDialog create(String id) {
        AnimationEditorDialog dialog = new AnimationEditorDialog();
        Bundle args = new Bundle();
        args.putString("mode_id", id);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        ModeStore store = new ModeStore(requireContext());
        draft = store.library.find(requireArguments().getString("mode_id"));
        if (draft == null) return new AlertDialog.Builder(requireContext()).setMessage(R.string.keep_one_mode)
                .setPositiveButton(android.R.string.ok, null).create();
        if (state != null) {
            draft.animationType = state.getString("type", draft.animationType);
            draft.animationSizePercent = state.getInt("size", draft.animationSizePercent);
            draft.animationHeightDp = state.getInt("height", draft.animationHeightDp);
            draft.animationDurationMs = state.getInt("duration", draft.animationDurationMs);
            draft.fullscreen = state.getBoolean("fullscreen", draft.fullscreen);
            draft.animationColor = state.getString("color", draft.animationColor);
            draft.backgroundColor = state.getString("background", draft.backgroundColor);
        }
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(8), dp(24), dp(16));
        previewFrame = new FrameLayout(requireContext());
        previewFrame.setBackgroundColor(Color.parseColor("#F5F1F3"));
        preview = new HypnosisAnimationView(requireContext());
        previewFrame.addView(preview, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));
        previewFrame.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            if (r - l != or - ol || b - t != ob - ot) updatePreview();
        });
        label(content, R.string.animation_preview_hint);
        label(content, R.string.animation_type);
        Spinner type = new Spinner(requireContext());
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.animation_names,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);
        String[] types = getResources().getStringArray(R.array.animation_values);
        for (int i = 0; i < types.length; i++) if (types[i].equals(draft.animationType)) type.setSelection(i);
        content.addView(type, new LinearLayout.LayoutParams(-1, dp(48)));
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                draft.animationType = types[position];
                updatePreview();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        slider(content, R.string.animation_size, 10, 100, draft.animationSizePercent, value -> draft.animationSizePercent = value);
        slider(content, R.string.animation_height, 120, 800, draft.animationHeightDp, value -> draft.animationHeightDp = value);
        slider(content, R.string.animation_duration, 1000, 20000, draft.animationDurationMs, value -> draft.animationDurationMs = value);
        SwitchCompat fullscreen = new SwitchCompat(requireContext());
        fullscreen.setText(R.string.fullscreen_playback);
        fullscreen.setTextSize(14);
        fullscreen.setChecked(draft.fullscreen);
        fullscreen.setTrackResource(R.drawable.switch_track);
        fullscreen.setThumbResource(R.drawable.switch_thumb);
        fullscreen.setTrackTintList(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.switch_track));
        fullscreen.setThumbTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        content.addView(fullscreen, new LinearLayout.LayoutParams(-1, dp(56)));
        fullscreen.setOnCheckedChangeListener((button, checked) -> { draft.fullscreen = checked; updatePreview(); });
        foreground = colorField(content, R.string.animation_color, state == null ? draft.animationColor : state.getString("color_input", draft.animationColor), true);
        background = colorField(content, R.string.background_color, state == null ? draft.backgroundColor : state.getString("background_input", draft.backgroundColor), false);
        ScrollView scroll = new ScrollView(requireContext());
        scroll.addView(content);
        LinearLayout panel = new LinearLayout(requireContext());
        panel.setOrientation(LinearLayout.VERTICAL);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        LinearLayout.LayoutParams previewLayout = new LinearLayout.LayoutParams(-1, Math.min(dp(220), screenHeight / 4));
        previewLayout.setMargins(dp(24), dp(8), dp(24), 0);
        panel.addView(previewFrame, previewLayout);
        panel.addView(scroll, new LinearLayout.LayoutParams(-1, screenHeight / 3));
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle(R.string.animation_settings)
                .setView(panel).setNegativeButton(android.R.string.cancel, null).setPositiveButton(R.string.save, null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> save()));
        updatePreview();
        return dialog;
    }

    private interface ValueChange { void set(int value); }

    private void slider(LinearLayout content, int title, int min, int max, int initial, ValueChange change) {
        TextView value = label(content, title);
        value.setText(getString(R.string.parameter_value, getString(title), String.valueOf(initial)));
        SeekBar slider = (SeekBar) android.view.LayoutInflater.from(requireContext()).inflate(R.layout.minimal_slider, content, false);
        slider.setMax(max - min);
        slider.setProgress(initial - min);
        slider.setContentDescription(value.getText());
        content.addView(slider);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                change.set(min + progress);
                value.setText(getString(R.string.parameter_value, getString(title), String.valueOf(min + progress)));
                bar.setContentDescription(value.getText());
                updatePreview();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });
    }

    private EditText colorField(LinearLayout content, int title, String initial, boolean isForeground) {
        label(content, title);
        EditText field = new AppCompatEditText(requireContext());
        field.setSingleLine(true);
        field.setSaveEnabled(false);
        field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
        field.setText(initial);
        field.setTextSize(14);
        field.setContentDescription(getString(title));
        content.addView(field, new LinearLayout.LayoutParams(-1, dp(48)));
        field.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().matches("#[0-9a-fA-F]{6}")) {
                    if (isForeground) draft.animationColor = s.toString();
                    else draft.backgroundColor = s.toString();
                    field.setError(null);
                    updatePreview();
                }
            }
            @Override public void afterTextChanged(Editable text) { }
        });
        return field;
    }

    private TextView label(LinearLayout content, int title) {
        TextView label = new TextView(requireContext());
        label.setText(title);
        label.setTextSize(13);
        label.setTextColor(Color.parseColor("#715A65"));
        label.setPadding(0, dp(16), 0, dp(4));
        content.addView(label);
        return label;
    }

    private void updatePreview() {
        if (preview == null || draft == null) return;
        preview.configure(draft);
        int width = previewFrame.getWidth(), height = previewFrame.getHeight();
        if (width == 0 || height == 0) return;
        android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
        float targetWidth = draft.fullscreen ? metrics.widthPixels : Math.max(dp(120), metrics.widthPixels - dp(48));
        float targetHeight = draft.fullscreen ? metrics.heightPixels : dp(draft.animationHeightDp);
        float scale = Math.min(width / targetWidth, height / targetHeight);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) preview.getLayoutParams();
        int w = Math.max(1, Math.round(targetWidth * scale)), h = Math.max(1, Math.round(targetHeight * scale));
        if (params.width != w || params.height != h) {
            params.width = w; params.height = h;
            preview.setLayoutParams(params);
        }
    }

    private void save() {
        boolean valid = true;
        for (EditText field : new EditText[]{foreground, background}) {
            if (!field.getText().toString().matches("#[0-9a-fA-F]{6}")) {
                field.setError(getString(R.string.color_help)); valid = false;
            }
        }
        if (!valid) return;
        ModeStore store = new ModeStore(requireContext());
        ModeConfig mode = store.library.find(draft.id);
        if (mode != null) {
            mode.animationType = draft.animationType;
            mode.animationSizePercent = draft.animationSizePercent;
            mode.animationHeightDp = draft.animationHeightDp;
            mode.animationDurationMs = draft.animationDurationMs;
            mode.animationColor = draft.animationColor;
            mode.backgroundColor = draft.backgroundColor;
            mode.fullscreen = draft.fullscreen;
            store.save();
            ModeFormDialog.updateHost(getParentFragment());
        }
        dismiss();
    }

    @Override public void onResume() { super.onResume(); if (preview != null) preview.start(); }
    @Override public void onPause() { if (preview != null) preview.stop(); super.onPause(); }
    @Override public void onDestroyView() {
        if (preview != null) preview.stop();
        preview = null; previewFrame = null;
        super.onDestroyView();
    }
    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        if (draft == null) return;
        state.putString("type", draft.animationType);
        state.putInt("size", draft.animationSizePercent);
        state.putInt("height", draft.animationHeightDp);
        state.putInt("duration", draft.animationDurationMs);
        state.putBoolean("fullscreen", draft.fullscreen);
        state.putString("color", draft.animationColor);
        state.putString("background", draft.backgroundColor);
        state.putString("color_input", foreground.getText().toString());
        state.putString("background_input", background.getText().toString());
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
