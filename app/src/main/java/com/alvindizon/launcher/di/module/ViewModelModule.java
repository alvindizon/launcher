package com.alvindizon.launcher.di.module;

import android.content.pm.PackageManager;

import androidx.lifecycle.ViewModel;

import com.alvindizon.launcher.features.main.MainViewModel;
import com.alvindizon.launcher.core.ViewModelFactory;
import com.alvindizon.launcher.data.FaveAppRepository;

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
    @ViewModelKey(MainViewModel.class)
    ViewModel provideMainViewModel(FaveAppRepository faveAppRepository, PackageManager packageManager) {
        return new MainViewModel(faveAppRepository, packageManager);
    }

}
