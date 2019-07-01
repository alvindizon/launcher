package com.alvindizon.launcher;

import android.content.Context;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
    private NavController navController;

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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        navController = ((MainActivity) requireActivity()).getNavController();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.button.setOnClickListener((v -> addFavorites()));
        binding.toolbar.inflateMenu(R.menu.menu_add_apps);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_disp_add_apps:
                    navController.navigate(R.id.action_favorites_dest_to_app_list_dest);
                    return true;
                default:
                    break;
            }
            return super.onOptionsItemSelected(item);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void addFavorites() {
        navController.navigate(R.id.action_favorites_dest_to_app_list_dest);
    }
}
