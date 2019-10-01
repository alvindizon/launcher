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

    public String get(int key, String defaultValue) {
        return sharedPreferences.getString(context.getResources().getString(key), defaultValue);
    }

    public boolean get(int key, boolean defaultValue) {
        return sharedPreferences.getBoolean(context.getResources().getString(key), defaultValue);
    }

    public void set(int key, String value) {
        sharedPreferences.edit().putString(context.getResources().getString(key), value).apply();
    }

    public void set(int key, int value) {
        sharedPreferences.edit().putInt(context.getString(key), value).apply();
    }

    public void set(int key, boolean value) {
        sharedPreferences.edit().putBoolean(context.getString(key), value).apply();
    }

    public int get(int key, int defaultValue) {
        return sharedPreferences.getInt(context.getString(key), defaultValue);
    }
}
