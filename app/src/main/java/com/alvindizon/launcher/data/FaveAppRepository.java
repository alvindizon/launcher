package com.alvindizon.launcher.data;

import com.alvindizon.launcher.di.module.FaveAppDao;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Singleton
public class FaveAppRepository {

    private final FaveAppDao faveAppDao;

    @Inject
    public FaveAppRepository(FaveAppDao faveAppDao) {
        this.faveAppDao = faveAppDao;
    }

    public Single<Long> createRecord(FaveAppRecord record) {
        return faveAppDao.addFaveAppRecord(record);
    }

    public Completable createRecordCompletable(FaveAppRecord record) {
        return faveAppDao.addFaveAppRecordCompletable(record);
    }

    public Completable clearFaveAppDb() {
        return faveAppDao.clearFaveAppDb();
    }

    public Maybe<List<FaveAppRecord>> getFaveApps() {
        return faveAppDao.getAllRecords();
    }

    public Completable deleteRecordByName(String packageName) {
        return faveAppDao.deleteRecordByPackageName(packageName);
    }
}
