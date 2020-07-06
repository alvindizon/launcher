package com.alvindizon.launcher.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;

@Dao
public abstract class FaveAppDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(FaveAppRecord record);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract List<Long> insert(List<FaveAppRecord> record);

    @Query("DELETE FROM faveapps")
    public abstract Completable clearFaveAppDb();

    @Query("SELECT * FROM faveapps")
    public abstract List<FaveAppRecord> getAllRecords();

    @Query("DELETE FROM faveapps WHERE packageName like :packageName")
    public abstract void deleteRecordByPackageName(String packageName);
}
