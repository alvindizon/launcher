package com.alvindizon.launcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private List<AppModel> appList = new ArrayList<>();

    private PackageManager packageManager;
    private AppListAdapter appListAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        packageManager = getApplicationContext().getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resInfos = packageManager.queryIntentActivities(intent, 0);

        addToAppList(resInfos);

        appListAdapter = new AppListAdapter(this::launchApp);
        recyclerView = findViewById(R.id.rv_nav);

        appListAdapter.setAppList(appList);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.recycler_horizontal_bottom_border)));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(itemDecorator);
        recyclerView.setAdapter(appListAdapter);

    }

    private void addToAppList(List<ResolveInfo> resInfos) {
        HashSet<AppModel> appSet = new HashSet<>();

        for(ResolveInfo resolveInfo : resInfos) {
            if(packageManager.getApplicationLabel(resolveInfo.activityInfo.applicationInfo).toString()
                    .equals(getString(R.string.app_name))) {
                continue;
            }
            AppModel appModel = new AppModel();
            appModel.setPackageName(resolveInfo.activityInfo.packageName);
            appModel.setAppLabel(packageManager.getApplicationLabel(resolveInfo.activityInfo.applicationInfo).toString());
            appModel.setLauncherIcon(packageManager.getApplicationIcon(resolveInfo.activityInfo.applicationInfo));
            appSet.add(appModel);
        }

        appList.addAll(appSet);
    }

    private void launchApp(String packageName) {

        try {
            startActivity(packageManager.getLaunchIntentForPackage(packageName));
        } catch (Exception e) {
            Toast.makeText(
                    MainActivity.this,
                    String.format("Error: Couldn't launch app: %s", packageName),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
