package com.alvindizon.launcher.features.faveapps;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alvindizon.launcher.core.AppModel;
import com.alvindizon.launcher.application.MainActivity;
import com.alvindizon.launcher.R;
import com.alvindizon.launcher.databinding.FragmentFavoritesBinding;
import com.squareup.moshi.JsonAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.alvindizon.launcher.application.MainActivity.FAVE_LIST;

public class FavoritesFragment extends Fragment {
    public static final String TAG = FavoritesFragment.class.getSimpleName();

    private PackageManager packageManager;
    private FaveListAdapter faveListAdapter;
    FragmentFavoritesBinding binding;
    private List<AppModel> faveList = new ArrayList<>();
    private List<String> favePackageNameList = new ArrayList<>();
    private NavController navController;
    private SharedPreferences preferences;
    JsonAdapter<List<String>> faveAppJsonAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);

        navController = ((MainActivity) requireActivity()).getNavController();
        packageManager = requireActivity().getPackageManager();
        preferences = ((MainActivity) requireActivity()).getCustomSharedPrefs();
        faveAppJsonAdapter = ((MainActivity) requireActivity()).getFaveAppJsonAdapter();

        loadFaveAppList();
        initRecyclerView();
        return binding.getRoot();
    }

    private void initRecyclerView() {
        if(faveList.isEmpty()) {
            binding.button.setVisibility(View.VISIBLE);
            binding.frameFav.setVisibility(View.GONE);
        } else {
            binding.button.setVisibility(View.GONE);
            binding.frameFav.setVisibility(View.VISIBLE);
        }

        faveListAdapter = new FaveListAdapter(this::launchApp);
        faveListAdapter.setAppList(faveList);

        faveListAdapter.setOnDeleteItemListener(list -> {
            faveList = list;
            saveFaveAppListToPrefs(faveList);
            if(faveList.isEmpty()) {
                binding.button.setVisibility(View.VISIBLE);
                binding.frameFav.setVisibility(View.GONE);
            } else {
                binding.button.setVisibility(View.GONE);
                binding.frameFav.setVisibility(View.VISIBLE);
            }
        });

        DividerItemDecoration itemDecorator = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.recycler_horizontal_bottom_border)));
        binding.rvNav.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNav.addItemDecoration(itemDecorator);
        binding.rvNav.setAdapter(faveListAdapter);
    }

    private void loadFaveAppList() {
        String faveAppListJsonString = preferences.getString(FAVE_LIST, "");
        if (!TextUtils.isEmpty(faveAppListJsonString)) {
            try {
                favePackageNameList = faveAppJsonAdapter.fromJson(faveAppListJsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // transform favePackageNameList to appmodels
        if (favePackageNameList != null && !favePackageNameList.isEmpty()) {
            for (String packageName : favePackageNameList) {
                Log.d(TAG, packageName);
                AppModel appModel = new AppModel();
                ApplicationInfo appInfo;
                try {
                    appInfo = packageManager.getApplicationInfo(packageName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    return;
                }

                appModel.setPackageName(appInfo.packageName);
                appModel.setLauncherIcon(packageManager.getApplicationIcon(appInfo));
                appModel.setAppLabel(packageManager.getApplicationLabel(appInfo).toString());
                faveList.add(appModel);
            }
        }
    }

    private void saveFaveAppListToPrefs(List<AppModel> faveAppList) {
        List<String> favePackageNameList = new ArrayList<>();

        for(AppModel appModel : faveAppList) {
            favePackageNameList.add(appModel.getPackageName());
        }

        String faveAppListJson = faveAppJsonAdapter.toJson(favePackageNameList);
        preferences.edit().putString(FAVE_LIST, faveAppListJson).apply();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // do nothing on back press
                    }
                });

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

    private void addFavorites() {
        navController.navigate(R.id.action_favorites_dest_to_app_list_dest);
    }

    private void launchApp(String packageName) {
        try {
            startActivity(packageManager.getLaunchIntentForPackage(packageName));
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Could not launch app \" " + packageName + "\"", Toast.LENGTH_LONG).show();
        }
    }
}
