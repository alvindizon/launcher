package com.alvindizon.launcher.di.component;

import com.alvindizon.launcher.application.MainActivity;
import com.alvindizon.launcher.di.module.ViewModelModule;
import com.alvindizon.launcher.features.applist.AppListFragment;
import com.alvindizon.launcher.features.favorites.GridListFragment;
import com.alvindizon.launcher.features.favorites.VerticalListFragment;

import dagger.Subcomponent;

@Subcomponent(modules = ViewModelModule.class)
public interface ViewModelComponent {
    void inject(MainActivity activity);
    void inject(AppListFragment fragment);
    void inject(VerticalListFragment fragment);
    void inject(GridListFragment fragment);
}
