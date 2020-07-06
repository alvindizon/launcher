package com.alvindizon.launcher;

import android.app.Application;

import com.alvindizon.launcher.di.component.DaggerAppComponent;
import com.alvindizon.launcher.di.component.AppComponent;
import com.alvindizon.launcher.di.module.ApplicationModule;

public class LauncherApp extends Application {
    private static LauncherApp INSTANCE;

    AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        appComponent = DaggerAppComponent.builder()
                            .applicationModule(new ApplicationModule(this))
                            .build();
    }

    public static LauncherApp get() {
        return INSTANCE;
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
