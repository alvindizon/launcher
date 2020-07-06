package com.alvindizon.launcher.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {FaveAppRecord.class}, version = 1)
public abstract class FaveAppDb extends RoomDatabase {

    public abstract FaveAppDao faveAppDao();
}
