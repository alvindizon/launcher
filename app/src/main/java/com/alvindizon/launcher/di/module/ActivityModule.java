package com.alvindizon.launcher.di.module;

import android.content.pm.PackageManager;

import androidx.fragment.app.FragmentActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

    private final FragmentActivity activity;

    public ActivityModule(FragmentActivity activity) {
        this.activity = activity;
    }

    @Provides
    PackageManager providePackageManager() {
        return activity.getPackageManager();
    }

}
