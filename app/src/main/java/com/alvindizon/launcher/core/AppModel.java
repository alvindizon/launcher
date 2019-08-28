package com.alvindizon.launcher.core;

import android.graphics.drawable.Drawable;

public class AppModel {

    private String packageName;

    private String appLabel;

    private Drawable launcherIcon;

    public AppModel(String packageName, String appLabel, Drawable launcherIcon) {
        this.packageName = packageName;
        this.appLabel = appLabel;
        this.launcherIcon = launcherIcon;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }

    public void setLauncherIcon(Drawable launcherIcon) {
        this.launcherIcon = launcherIcon;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public Drawable getLauncherIcon() {
        return launcherIcon;
    }

    @Override
    public String toString() {
        return "AppModel{" +
                "packageName='" + packageName + '\'' +
                ", appLabel='" + appLabel + '\'' +
                ", launcherIcon=" + launcherIcon +
                '}';
    }
}
