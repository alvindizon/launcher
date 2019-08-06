package com.alvindizon.launcher.features.applist;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alvindizon.launcher.core.AppModel;
import com.alvindizon.launcher.application.MainActivity;
import com.alvindizon.launcher.R;
import com.alvindizon.launcher.databinding.FragmentAppListBinding;
import com.squareup.moshi.JsonAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.alvindizon.launcher.application.MainActivity.FAVE_LIST;

public class AppListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<AppModel>> {
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


        DividerItemDecoration itemDecorator = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.recycler_horizontal_bottom_border)));
        binding.rvNav.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNav.addItemDecoration(itemDecorator);
        binding.rvNav.setAdapter(appListAdapter);
        LoaderManager.getInstance(this).initLoader(0, null, this);
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

    @NonNull
    @Override
    public Loader<List<AppModel>> onCreateLoader(int id, @Nullable Bundle args) {
        return new AppListLoader(requireContext(), packageManager);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<AppModel>> loader, List<AppModel> data) {
        appList = data;
        appListAdapter.setAppList(appList);
        binding.progressBar.setVisibility(View.GONE);
        binding.listContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<AppModel>> loader) {

    }
}
