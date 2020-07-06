package com.alvindizon.launcher.di;

import com.alvindizon.launcher.LauncherApp;
import com.alvindizon.launcher.di.component.AppComponent;

public class Injector {
    public static AppComponent get() {
        return LauncherApp.get().getAppComponent();
    }
}
