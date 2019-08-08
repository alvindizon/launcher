package com.alvindizon.launcher.di.module;

import androidx.lifecycle.ViewModel;

import com.alvindizon.launcher.core.PreferenceRepository;
import com.alvindizon.launcher.core.ViewModelFactory;
import com.alvindizon.launcher.features.applist.AppListViewModel;
import com.alvindizon.launcher.features.faveapps.FavoritesViewModel;
import com.squareup.moshi.Moshi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import javax.inject.Provider;

import dagger.MapKey;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module
public class ViewModelModule {
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @MapKey
    @interface ViewModelKey {
        Class<? extends ViewModel> value();
    }

    @Provides
    ViewModelFactory provideViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> providerMap) {
        return new ViewModelFactory(providerMap);
    }

    @Provides
    @IntoMap
    @ViewModelKey(AppListViewModel.class)
    ViewModel provideAppListViewModel(PreferenceRepository preferenceRepository, Moshi moshi) {
        return new AppListViewModel(preferenceRepository, moshi);
    }

    @Provides
    @IntoMap
    @ViewModelKey(FavoritesViewModel.class)
    ViewModel provideFavoritesViewModel(PreferenceRepository preferenceRepository, Moshi moshi) {
        return new FavoritesViewModel(preferenceRepository, moshi);
    }

}
