package com.reedoverflow.saiminapp.ui.home.basicmode;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BaiscViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public BaiscViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is basic fragment");
    }

    public MutableLiveData<String> getmText() {
        return mText;
    }

    public void setmText(MutableLiveData<String> mText) {
        this.mText = mText;
    }
}