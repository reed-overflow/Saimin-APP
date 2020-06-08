package com.reedoverflow.saiminapp.ui.home.extendmode;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.reedoverflow.saiminapp.R;
import com.reedoverflow.saiminapp.ui.home.basicmode.BaiscFragment;
import com.reedoverflow.saiminapp.ui.home.basicmode.BaiscViewModel;
import com.reedoverflow.saiminapp.utils.RippleBackground;
import com.wuyr.rippleanimation.RippleAnimation;

public class ExtendFragment extends BaiscFragment {

    private BaiscViewModel baiscViewModel;
    private SwitchButton homeSwitch;
    private RippleBackground rippleBackground;
    private CardView cardView;

    public static ExtendFragment newInstance() {
        ExtendFragment fragment = new ExtendFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        baiscViewModel = ViewModelProviders.of(this).get(BaiscViewModel.class);
        View root = inflater.inflate(R.layout.fragment_extend, container, false);

        final TextView homeText = root.findViewById(R.id.home_text);
        rippleBackground = root.findViewById(R.id.ripple_bg);
        homeSwitch = root.findViewById(R.id.home_switch);

        baiscViewModel.getHomeText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                homeText.setText(s);
            }
        });
        baiscViewModel.getHomeText().postValue(getString(R.string.basic_saimin_stop));

        cardView = root.findViewById(R.id.sensitive_card_view);


        homeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    saiminStart();
                } else {
                    RippleAnimation.create(homeSwitch).setDuration(1000).start();
                    saiminStop();
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.card_bg));
                }
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        baiscViewModel = ViewModelProviders.of(this).get(BaiscViewModel.class);
    }

    //临时模拟
    private void saiminStart() {
        baiscViewModel.getHomeText().postValue(getString(R.string.basic_saimin_starting));
        homeSwitch.setEnabled(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RippleAnimation.create(homeSwitch).setDuration(3000).start();
                cardView.setCardBackgroundColor(getResources().getColor(R.color.card_bg_alter));

                baiscViewModel.getHomeText().postValue(getString(R.string.basic_saimin_start));
                homeSwitch.setEnabled(true);

                rippleBackground.startRippleAnimation();
            }
        }, 2000);
    }

    //临时模拟
    private void saiminStop() {
        baiscViewModel.getHomeText().postValue(getString(R.string.basic_saimin_shutdown));
        homeSwitch.setEnabled(false);
        rippleBackground.stopRippleAnimation();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                baiscViewModel.getHomeText().postValue(getString(R.string.basic_saimin_stop));
                homeSwitch.setEnabled(true);
            }
        }, 1000);
    }
}