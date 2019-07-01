package com.alvindizon.launcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

import com.alvindizon.launcher.databinding.FragmentAppListBinding;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.alvindizon.launcher.MainActivity.FAVE_LIST;

public class AppListFragment extends Fragment {
    public static final String TAG = AppListFragment.class.getSimpleName();

    private List<AppModel> appList = new ArrayList<>();
    private List<AppModel> faveAppList = new ArrayList<>();


    private PackageManager packageManager;
    private AppListAdapter appListAdapter;
    FragmentAppListBinding binding;
    private NavController navController;
    private SharedPreferences preferences;
    JsonAdapter<List<String>> faveAppJsonAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAppListBinding.inflate(inflater, container, false);

        navController = ((MainActivity) requireActivity()).getNavController();
        packageManager = requireActivity().getPackageManager();
        preferences = ((MainActivity) requireActivity()).getCustomSharedPrefs();
        faveAppJsonAdapter = ((MainActivity) requireActivity()).getFaveAppJsonAdapter();

        for(ResolveInfo resolveInfo : getLaunchableActivities()) {
            String appLabel =(String) resolveInfo.loadLabel(packageManager);
            if(appLabel.equals(getString(R.string.app_name))) {
                continue;
            }
            AppModel appModel = new AppModel();
            appModel.setPackageName(resolveInfo.activityInfo.packageName);
            appModel.setAppLabel(appLabel);
            appModel.setLauncherIcon(packageManager.getApplicationIcon(resolveInfo.activityInfo.applicationInfo));
            appList.add(appModel);
        }

        appListAdapter = new AppListAdapter(new AppListAdapter.AppItemListener() {
            @Override
            public void onItemClick(AppModel app) {
                Log.d(TAG, app.getPackageName());
                faveAppList.add(app);
            }

            @Override
            public void onItemUncheck(AppModel app) {
                faveAppList.remove(app);
            }
        });
        appListAdapter.setAppList(appList);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.recycler_horizontal_bottom_border)));
        binding.rvNav.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNav.addItemDecoration(itemDecorator);
        binding.rvNav.setAdapter(appListAdapter);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        saveFaveAppListToPrefs(faveAppList);
                        navController.navigate(R.id.action_app_list_dest_to_favorites_dest);
                    }
                });
    }

    private List<ResolveInfo> getLaunchableActivities() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resInfos = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(resInfos, new ResolveInfo.DisplayNameComparator(packageManager));
        return resInfos;
    }

    private void saveFaveAppListToPrefs(List<AppModel> faveAppList) {
        List<String> favePackageNameList = new ArrayList<>();


        String faveAppListJsonString = preferences.getString(FAVE_LIST, "");
        // get existing fave list if it exists
        if (!TextUtils.isEmpty(faveAppListJsonString)) {
            try {
                favePackageNameList = faveAppJsonAdapter.fromJson(faveAppListJsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(AppModel appModel : faveAppList) {
            favePackageNameList.add(appModel.getPackageName());
        }

        String faveAppListJson = faveAppJsonAdapter.toJson(favePackageNameList);
        preferences.edit().putString(FAVE_LIST, faveAppListJson).apply();
    }

}
