package com.reedoverflow.saiminapp.ui.home.basicmode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.reedoverflow.saiminapp.R;

public class BaiscFragment extends Fragment {

    private BaiscViewModel baiscViewModel;
    private Switch homeSwitch;

    public static BaiscFragment newInstance() {
        return new BaiscFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        baiscViewModel = ViewModelProviders.of(this).get(BaiscViewModel.class);
        View root = inflater.inflate(R.layout.fragment_baisc, container, false);

        final TextView homeText = root.findViewById(R.id.home_text);
        baiscViewModel.getHomeText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                homeText.setText(s);
            }
        });

        baiscViewModel.getHomeText().postValue(getString(R.string.basic_saimin_stop));

        homeSwitch = root.findViewById(R.id.home_switch);
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

    // TODO: 2020/6/3 动画效果
    //临时模拟
    private void saiminStart() {
        baiscViewModel.getHomeText().postValue(getString(R.string.basic_saimin_starting));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    baiscViewModel.getHomeText().postValue(getString(R.string.basic_saimin_start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //临时模拟
    private void saiminStop() {
        baiscViewModel.getHomeText().postValue(getString(R.string.basic_saimin_shutdown));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    baiscViewModel.getHomeText().postValue(getString(R.string.basic_saimin_stop));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}