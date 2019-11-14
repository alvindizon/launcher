package com.alvindizon.launcher.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "faveapps", indices = {@Index(value = "id", unique = true)})
public class FaveAppRecord {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    private String packageName;

    private String appLabel;

    public FaveAppRecord(String packageName, String appLabel) {
        this.packageName = packageName;
        this.appLabel = appLabel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }
}
