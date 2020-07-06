package com.alvindizon.launcher.di.component;

import com.alvindizon.launcher.di.module.ActivityModule;
import com.alvindizon.launcher.di.module.ApplicationModule;
import com.alvindizon.launcher.di.module.DatabaseModule;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {ApplicationModule.class,
                      DatabaseModule.class})
@Singleton
public interface AppComponent {
    ActivityComponent activityComponent(ActivityModule activityModule);
}
