package com.alvindizon.launcher;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alvindizon.launcher.databinding.FragmentFavoritesBinding;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {
    public static final String FAVORITES_KEY = "key_favorites";
    public static final String SEPARATOR = ",";

    private PackageManager packageManager;
    private AppListAdapter appListAdapter;
    FragmentFavoritesBinding binding;
    private SharedPreferences preferences;
    private List<AppModel> faveList = new ArrayList<>();
    private List<String> packageNameList = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);

        if(faveList.isEmpty()) {
            binding.button.setVisibility(View.VISIBLE);
            binding.frameFav.setVisibility(View.GONE);
        } else {
            binding.button.setVisibility(View.GONE);
            binding.frameFav.setVisibility(View.VISIBLE);
        }

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.button.setOnClickListener((v -> addFavorites()));
    }

    private void addFavorites() {
        preferences.edit().putString(FAVORITES_KEY, TextUtils.join(SEPARATOR, packageNameList)).commit();
    }
}
