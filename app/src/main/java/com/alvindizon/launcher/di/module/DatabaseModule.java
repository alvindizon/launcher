package com.alvindizon.launcher.di.module;

import android.content.Context;
import androidx.room.Room;
import com.alvindizon.launcher.data.FaveAppDb;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;

import static com.alvindizon.launcher.core.Const.APP_NAME;

@Module
public class DatabaseModule {

    // Note: for example, if we upgrade our app from version 1.0.0 to 1.0.1, and we added new
    // columns to our DB, we need to upgrade our DB version as well
    // fallbackToDestructiveMigration() ensures that the DB is CLEARED on DB upgrade.
    @Singleton
    @Provides
    FaveAppDb provideFaveAppRecord(Context appContext) {
        return Room.databaseBuilder(appContext, FaveAppDb.class, APP_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    FaveAppDao provideTransactionDao(FaveAppDb faveAppDb) {
        return faveAppDb.faveAppDao();
    }
}
