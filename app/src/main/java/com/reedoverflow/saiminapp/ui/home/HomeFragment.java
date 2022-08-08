package com.reedoverflow.saiminapp.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.reedoverflow.saiminapp.R;
import com.reedoverflow.saiminapp.ui.home.basicmode.BaiscFragment;
import com.reedoverflow.saiminapp.ui.home.extendmode.ExtendFragment;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        checkMode();
        return root;
    }

    private void checkMode() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        int option = preferences.getInt("switch", 1);

        switch (option) {
            case 0: {
                toFragment(BaiscFragment.newInstance());
                break;
            }
            case 1: {
                toFragment(ExtendFragment.newInstance());
                break;
            }
            default:{
                toFragment(ExtendFragment.newInstance());
                break;
            }
        }
    }

    private void toFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.fragment_cont, fragment).commit();
    }
}