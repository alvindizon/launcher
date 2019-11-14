package com.alvindizon.launcher.di.module;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.alvindizon.launcher.data.FaveAppRecord;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface FaveAppDao {

    @Insert(onConflict = REPLACE)
    Single<Long> addFaveAppRecord(FaveAppRecord record);

    @Insert(onConflict = REPLACE)
    Completable addFaveAppRecordCompletable(FaveAppRecord record);

    @Query("DELETE FROM faveapps")
    Completable clearFaveAppDb();

    @Query("SELECT * FROM faveapps")
    Maybe<List<FaveAppRecord>> getAllRecords();

    @Query("DELETE FROM faveapps WHERE packageName like :packageName")
    Completable deleteRecordByPackageName(String packageName);

}
