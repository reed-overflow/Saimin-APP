package com.reedoverflow.saiminapp.ui.switchmode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.reedoverflow.saiminapp.R;

// TODO: 2020/6/2 切换界面 
public class SwitchFragment extends Fragment {

    private SwitchViewModel mViewModel;

    public static SwitchFragment newInstance() {
        return new SwitchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_switch, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SwitchViewModel.class);
        // TODO: Use the ViewModel
    }

}