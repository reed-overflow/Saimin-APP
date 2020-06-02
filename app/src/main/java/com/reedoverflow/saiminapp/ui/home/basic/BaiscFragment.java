package com.reedoverflow.saiminapp.ui.home.basic;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.reedoverflow.saiminapp.R;

// TODO: 2020/6/2 主界面 
public class BaiscFragment extends Fragment {

    private BaiscViewModel baiscViewModel;

    public static BaiscFragment newInstance() {
        return new BaiscFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        baiscViewModel = ViewModelProviders.of(this).get(BaiscViewModel.class);
        View root = inflater.inflate(R.layout.fragment_baisc, container, false);

        final TextView textView = root.findViewById(R.id.home_text);
        baiscViewModel.getmText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        baiscViewModel = ViewModelProviders.of(this).get(BaiscViewModel.class);
        // TODO: Use the ViewModel
    }

}