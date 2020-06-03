package com.reedoverflow.saiminapp.ui.home.sensitivemode;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reedoverflow.saiminapp.R;

public class SensitiveFragment extends Fragment {


    public SensitiveFragment() {
    }

    public static SensitiveFragment newInstance() {
        SensitiveFragment fragment = new SensitiveFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensitive, container, false);
    }
}