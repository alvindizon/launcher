package com.alvindizon.launcher.di.component;

import com.alvindizon.launcher.di.module.ActivityModule;
import com.alvindizon.launcher.di.module.ViewModelModule;
import com.alvindizon.launcher.features.favorites.GridListFragment;
import com.alvindizon.launcher.features.favorites.VerticalListFragment;
import com.alvindizon.launcher.features.main.MainActivity;

import dagger.Subcomponent;

@Subcomponent(modules = {ActivityModule.class, ViewModelModule.class})
public interface ActivityComponent {
    void inject(MainActivity activity);
    void inject(VerticalListFragment fragment);
    void inject(GridListFragment fragment);
}
