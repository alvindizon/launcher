package com.alvindizon.launcher.features.applist;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.alvindizon.launcher.core.AppModel;
import com.alvindizon.launcher.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListLoader extends AsyncTaskLoader<List<AppModel>> {

    private PackageManager packageManager;
    private List<AppModel> data = new ArrayList<>();

    public AppListLoader(@NonNull Context context, PackageManager packageManager) {
        super(context);
        this.packageManager = packageManager;
    }

    @Override
    protected void onStartLoading() {
        if(!data.isEmpty()) {
            deliverResult(data);
        }
        forceLoad();
    }

    @Nullable
    @Override
    public List<AppModel> loadInBackground() {

        for(ResolveInfo resolveInfo : getLaunchableActivities()) {
            String appLabel =(String) resolveInfo.loadLabel(packageManager);
            if(appLabel.equals(getContext().getString(R.string.app_name))) {
                continue;
            }
            AppModel appModel = new AppModel();
            appModel.setPackageName(resolveInfo.activityInfo.packageName);
            appModel.setAppLabel(appLabel);
            appModel.setLauncherIcon(packageManager.getApplicationIcon(resolveInfo.activityInfo.applicationInfo));
            data.add(appModel);
        }

        return data;
    }

    @Override
    public void deliverResult(@Nullable List<AppModel> data) {
        this.data = data;
        super.deliverResult(data);
    }

    private List<ResolveInfo> getLaunchableActivities() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resInfos = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(resInfos, new ResolveInfo.DisplayNameComparator(packageManager));
        return resInfos;
    }
}
