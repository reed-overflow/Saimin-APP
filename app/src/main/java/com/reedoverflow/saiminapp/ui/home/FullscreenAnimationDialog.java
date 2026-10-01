package com.reedoverflow.saiminapp.ui.home;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.reedoverflow.saiminapp.R;
import com.reedoverflow.saiminapp.data.ModeConfig;
import com.reedoverflow.saiminapp.data.ModeStore;

public class FullscreenAnimationDialog extends DialogFragment {
    private HypnosisAnimationView animation;

    public static FullscreenAnimationDialog create(String modeId) {
        FullscreenAnimationDialog dialog = new FullscreenAnimationDialog();
        Bundle args = new Bundle();
        args.putString("mode_id", modeId);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle state) {
        Dialog dialog = new Dialog(requireContext(), R.style.AppTheme_NoActionBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        FrameLayout content = new FrameLayout(requireContext());
        content.setBackgroundColor(Color.rgb(255, 240, 245));
        animation = new HypnosisAnimationView(requireContext());
        ModeStore store = new ModeStore(requireContext());
        ModeConfig mode = store.library.find(requireArguments().getString("mode_id"));
        if (mode == null) mode = store.library.selected();
        content.setBackgroundColor(Color.parseColor(mode.backgroundColor));
        animation.configure(mode);
        content.addView(animation, new FrameLayout.LayoutParams(-1, -1));
        androidx.appcompat.widget.AppCompatButton exit = new androidx.appcompat.widget.AppCompatButton(requireContext());
        exit.setBackgroundResource(R.drawable.soft_button);
        androidx.core.view.ViewCompat.setBackgroundTintList(exit, null);
        exit.setTextColor(Color.parseColor("#A34B6D"));
        exit.setAllCaps(false);
        exit.setTextSize(13);
        exit.setStateListAnimator(null);
        exit.setMinHeight((int) (48 * getResources().getDisplayMetrics().density));
        exit.setText(R.string.exit_fullscreen);
        exit.setOnClickListener(view -> dismiss());
        FrameLayout.LayoutParams buttonLayout = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        buttonLayout.bottomMargin = (int) (40 * getResources().getDisplayMetrics().density);
        content.addView(exit, buttonLayout);
        dialog.setContentView(content);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog() == null ? null : getDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(-1, -1);
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        animation.start();
    }

    @Override
    public void onPause() {
        animation.stop();
        super.onPause();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (animation != null) animation.stop();
        super.onDismiss(dialog);
        if (getParentFragment() instanceof HomeFragment) ((HomeFragment) getParentFragment()).onFullscreenClosed();
    }
}
