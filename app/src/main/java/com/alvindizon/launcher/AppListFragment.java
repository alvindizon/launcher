package com.alvindizon.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alvindizon.launcher.databinding.FragmentAppListBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AppListFragment extends Fragment {

    private List<AppModel> appList = new ArrayList<>();

    private PackageManager packageManager;
    private AppListAdapter appListAdapter;
    FragmentAppListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAppListBinding.inflate(inflater, container, false);

        packageManager = requireActivity().getPackageManager();

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

        appListAdapter = new AppListAdapter(this::launchApp);
        appListAdapter.setAppList(appList);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.recycler_horizontal_bottom_border)));
        binding.rvNav.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNav.addItemDecoration(itemDecorator);
        binding.rvNav.setAdapter(appListAdapter);
        return binding.getRoot();
    }

    private List<ResolveInfo> getLaunchableActivities() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resInfos = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(resInfos, new ResolveInfo.DisplayNameComparator(packageManager));
        return resInfos;
    }

    private void launchApp(String packageName) {

        try {
            startActivity(packageManager.getLaunchIntentForPackage(packageName));
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Could not launch app \" " + packageName + "\"", Toast.LENGTH_LONG).show();
        }
    }
}
