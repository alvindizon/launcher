package com.alvindizon.launcher.di.module;

import android.app.Application;
import android.content.Context;

import com.squareup.moshi.Moshi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private final Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    Context provideApplicationContext() {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    Moshi provideMoshi() {
        return new Moshi.Builder().build();
    }
}
