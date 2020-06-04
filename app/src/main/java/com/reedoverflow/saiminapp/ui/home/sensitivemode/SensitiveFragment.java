package com.reedoverflow.saiminapp.ui.home.sensitivemode;

import android.os.Bundle;

import androidx.annotation.Nullable;
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

// TODO: 2020/6/3 This 
public class SensitiveFragment extends BaiscFragment {

    private BaiscViewModel baiscViewModel;
    private SwitchButton homeSwitch;
    private RippleBackground rippleBackground;

    public static SensitiveFragment newInstance() {
        SensitiveFragment fragment = new SensitiveFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        baiscViewModel = ViewModelProviders.of(this).get(BaiscViewModel.class);
        View root = inflater.inflate(R.layout.fragment_sensitive, container, false);

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

        homeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    saiminStart();
                } else {
                    saiminStop();
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                baiscViewModel.getHomeText().postValue(getString(R.string.basic_saimin_stop));
                homeSwitch.setEnabled(true);

                rippleBackground.stopRippleAnimation();
            }
        }, 1000);
    }
}