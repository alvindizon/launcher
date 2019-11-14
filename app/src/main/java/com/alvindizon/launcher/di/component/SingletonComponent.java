package com.alvindizon.launcher.di.component;

import android.content.Context;

import com.alvindizon.launcher.core.PreferenceHelper;
import com.alvindizon.launcher.di.module.ApplicationModule;
import com.alvindizon.launcher.di.module.DatabaseModule;
import com.alvindizon.launcher.di.module.ViewModelModule;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {ApplicationModule.class,
                      DatabaseModule.class})
@Singleton
public interface SingletonComponent {
    Context appContext();
    PreferenceHelper preferenceRepository();
    ViewModelComponent viewModelComponent(ViewModelModule viewModelModule);
}
