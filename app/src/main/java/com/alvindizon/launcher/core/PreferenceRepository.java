package com.alvindizon.launcher.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PreferenceRepository {

    private final SharedPreferences sharedPreferences;
    private final Context context;

    @Inject
    public PreferenceRepository(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String get(int key, int defaultValue) {
        return get(key, context.getString(defaultValue));
    }

    public String get(int key, String defaultValue) {
        return sharedPreferences.getString(context.getResources().getString(key), defaultValue);
    }

    private boolean get(int key, boolean defaultValue) {
        return sharedPreferences.getBoolean(context.getResources().getString(key), defaultValue);
    }

    public void set(int key, String value) {
        sharedPreferences.edit().putString(context.getResources().getString(key), value).apply();
    }
}
