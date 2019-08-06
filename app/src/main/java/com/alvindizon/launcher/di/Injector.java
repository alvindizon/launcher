package com.alvindizon.launcher.di;

import com.alvindizon.launcher.application.CustomApplication;
import com.alvindizon.launcher.di.component.SingletonComponent;
import com.alvindizon.launcher.di.component.ViewModelComponent;

public class Injector {
    public static SingletonComponent get() {
        return CustomApplication.get().getSingletonComponent();
    }

    public static ViewModelComponent getViewModelComponent() {
        return CustomApplication.get().getViewModelComponent();
    }
}
