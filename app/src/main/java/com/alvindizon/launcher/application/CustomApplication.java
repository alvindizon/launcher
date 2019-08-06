package com.alvindizon.launcher.application;

import android.app.Application;

import com.alvindizon.launcher.di.component.DaggerSingletonComponent;
import com.alvindizon.launcher.di.component.SingletonComponent;
import com.alvindizon.launcher.di.component.ViewModelComponent;
import com.alvindizon.launcher.di.module.ApplicationModule;
import com.alvindizon.launcher.di.module.ViewModelModule;

public class CustomApplication extends Application {

    private static CustomApplication INSTANCE;

    SingletonComponent singletonComponent;
    ViewModelComponent viewModelComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        singletonComponent = DaggerSingletonComponent.builder()
                            .applicationModule(new ApplicationModule(this))
                            .build();
        viewModelComponent = singletonComponent.viewModelComponent(new ViewModelModule());
    }

    public static CustomApplication get() {
        return INSTANCE;
    }

    public SingletonComponent getSingletonComponent() {
        return singletonComponent;
    }

    public ViewModelComponent getViewModelComponent() {
        return viewModelComponent;
    }
}
