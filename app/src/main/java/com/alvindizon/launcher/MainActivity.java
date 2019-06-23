package com.alvindizon.launcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
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
        List<PackageInfo> apps = packageManager.getInstalledPackages(0);

        addToAppList(apps);
        appListAdapter = new AppListAdapter(packageName -> launchApp(packageName));
        recyclerView = findViewById(R.id.rv_nav);
        appListAdapter.setAppList(appList);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.recycler_horizontal_bottom_border)));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(itemDecorator);
        recyclerView.setAdapter(appListAdapter);

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

    private void addToAppList(List<PackageInfo> apps) {
        for(PackageInfo pkgInfo : apps) {
            if((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                if(packageManager.getApplicationLabel(pkgInfo.applicationInfo).toString().equals(getString(R.string.app_name))) {
                    continue;
                }
                AppModel appModel = new AppModel();
                appModel.setPackageName(pkgInfo.packageName);
                appModel.setAppLabel(packageManager.getApplicationLabel(pkgInfo.applicationInfo).toString());
                appModel.setLauncherIcon(packageManager.getApplicationIcon(pkgInfo.applicationInfo));
                appList.add(appModel);
            }
        }
    }
}
