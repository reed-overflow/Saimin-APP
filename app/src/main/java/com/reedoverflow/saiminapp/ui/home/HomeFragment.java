package com.reedoverflow.saiminapp.ui.home;

import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.reedoverflow.saiminapp.R;
import com.reedoverflow.saiminapp.data.ModeConfig;
import com.reedoverflow.saiminapp.data.ModeStore;
import com.reedoverflow.saiminapp.data.ParameterConfig;
import org.json.JSONException;

public class HomeFragment extends Fragment {
    private HypnosisAnimationView animation;
    private SwitchCompat playSwitch;
    private ModeConfig mode;
    private String renderedConfig;
    private String displayedId;
    private Bundle progressValues = new Bundle();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        if (state != null) {
            displayedId = state.getString("mode_id");
            Bundle saved = state.getBundle("progress_values");
            if (saved != null) progressValues = saved;
        }
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle state) {
        animation = view.findViewById(R.id.custom_animation);
        playSwitch = view.findViewById(R.id.play_switch);
        playSwitch.setOnCheckedChangeListener((button, checked) -> {
            playSwitch.setContentDescription(getString(checked ? R.string.basic_saimin_start : R.string.basic_saimin_stop));
            if (checked && mode != null) {
                if (mode.fullscreen) {
                    if (getChildFragmentManager().findFragmentByTag("fullscreen") == null) {
                        FullscreenAnimationDialog.create(mode.id).show(getChildFragmentManager(), "fullscreen");
                    }
                } else animation.start();
            } else animation.stop();
        });
        view.findViewById(R.id.reset_parameters).setOnClickListener(button -> {
            progressValues.clear();
            renderParameters();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshMode();
        if (getChildFragmentManager().findFragmentByTag("fullscreen") != null) playSwitch.setChecked(true);
    }

    public void refreshMode() {
        if (getView() == null) return;
        ModeStore store = new ModeStore(requireContext());
        String config;
        try {
            config = store.library.toJson();
        } catch (JSONException exception) {
            throw new IllegalStateException(exception);
        }
        if (config.equals(renderedConfig)) return;
        ModeConfig selected = store.library.selected();
        if (!selected.id.equals(displayedId) || renderedConfig != null) progressValues.clear();
        displayedId = selected.id;
        renderedConfig = config;
        mode = selected;
        playSwitch.setChecked(false);
        ((TextView) requireView().findViewById(R.id.mode_title)).setText(mode.name);
        View card = requireView().findViewById(R.id.animation_card);
        ViewGroup.LayoutParams params = card.getLayoutParams();
        params.height = dp(mode.animationHeightDp);
        card.setLayoutParams(params);
        animation.configure(mode);
        renderParameters();
    }

    private void renderParameters() {
        if (mode == null) return;
        LinearLayout container = requireView().findViewById(R.id.custom_parameters);
        container.removeAllViews();
        for (ParameterConfig parameter : mode.parameters) {
            LinearLayout heading = new LinearLayout(requireContext());
            heading.setGravity(Gravity.CENTER_VERTICAL);
            heading.setPadding(0, dp(16), 0, 0);
            TextView label = new TextView(requireContext());
            label.setText(parameter.name);
            label.setTextSize(14);
            label.setTextColor(Color.parseColor("#715A65"));
            heading.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
            TextView value = new TextView(requireContext());
            value.setTextSize(14);
            value.setTextColor(Color.parseColor("#A34B6D"));
            value.setPadding(dp(16), 0, 0, 0);
            heading.addView(value);
            container.addView(heading);
            AppCompatSeekBar slider = (AppCompatSeekBar) getLayoutInflater().inflate(R.layout.minimal_slider, container, false);
            slider.setMax(parameter.stepCount());
            slider.setProgress(progressValues.getInt(parameter.id, parameter.defaultProgress()));
            updateLabel(value, slider, parameter);
            container.addView(slider, new LinearLayout.LayoutParams(-1, dp(48)));
            slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    progressValues.putInt(parameter.id, progress);
                    updateLabel(value, seekBar, parameter);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override public void onStopTrackingTouch(SeekBar seekBar) { }
            });
        }
        requireView().findViewById(R.id.reset_parameters).setVisibility(mode.parameters.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateLabel(TextView label, SeekBar slider, ParameterConfig parameter) {
        String value = getString(R.string.parameter_value, parameter.name, parameter.valueAt(slider.getProgress()));
        label.setText(parameter.valueAt(slider.getProgress()));
        slider.setContentDescription(value);
    }

    public void onFullscreenClosed() {
        if (playSwitch != null) playSwitch.setChecked(false);
    }

    @Override
    public void onPause() {
        if (playSwitch != null) playSwitch.setChecked(false);
        if (animation != null) animation.stop();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("mode_id", displayedId);
        state.putBundle("progress_values", progressValues);
    }

    @Override
    public void onDestroyView() {
        animation.stop();
        animation = null;
        playSwitch = null;
        renderedConfig = null;
        super.onDestroyView();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
