package com.reedoverflow.saiminapp.ui.home.basicmode;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BaiscViewModel extends ViewModel {
    private MutableLiveData<String> homeText;

    public BaiscViewModel() {
        homeText = new MutableLiveData<>();
    }

    public MutableLiveData<String> getHomeText() {
        return homeText;
    }
}