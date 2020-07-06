package com.alvindizon.launcher.features.main;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alvindizon.launcher.core.Const;
import com.alvindizon.launcher.core.applist.AppModel;
import com.alvindizon.launcher.data.FaveAppDao;
import com.alvindizon.launcher.data.FaveAppRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends ViewModel {

    private final FaveAppDao faveAppDao;

    private final PackageManager packageManager;

    private CompositeDisposable compositeDisposable;

    private MutableLiveData<List<AppModel>> listData = new MutableLiveData<>();

    @Inject
    public MainViewModel(FaveAppDao faveAppDao, PackageManager packageManager) {
        this.faveAppDao = faveAppDao;
        this.packageManager = packageManager;
        this.compositeDisposable = new CompositeDisposable();
    }

    public LiveData<List<AppModel>> getListData() {
        return listData;
    }

    public LiveData<List<AppModel>> getLaunchableApps() {
        MutableLiveData<List<AppModel>> appListData = new MutableLiveData<>();
        compositeDisposable.add(getLaunchableList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(appListData::setValue, Throwable::printStackTrace));
        return appListData;
    }

    public void loadFaveAppList() {
        compositeDisposable.add(Single.fromCallable(faveAppDao::getAllRecords)
                .flatMap(this::loadSavedFavoriteApps)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listData::setValue, Throwable::printStackTrace));
    }

    /**
     * Takes an AppModel object, converts it to FaveAppRecord, and inserts it
     * to the DB. If the app already exists in the faveapps DB, it is ignored and not added.
     * @param app An AppModel object that is
     */
    public void addFaveApp(AppModel app) {
        compositeDisposable.add(Completable.fromAction(() ->
                faveAppDao.insert(new FaveAppRecord(app.getPackageName(), app.getAppLabel()))
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> {}, Throwable::printStackTrace)
        );
    }

    /**
     * Removes an app from the faveapps DB via its package name.
     * @param app An app to be removed from the db
     */
    public void deleteFaveApp(AppModel app) {
        compositeDisposable.add(Completable.fromAction(() -> faveAppDao.deleteRecordByPackageName(app.getPackageName()))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> {}, Throwable::printStackTrace)
        );
    }

    private Single<List<AppModel>> getLaunchableList() {
        List<AppModel> data = new ArrayList<>();
        return Single.create(emitter -> {
            try {
                for(ResolveInfo resolveInfo : getLaunchableActivities()) {
                    String appLabel =(String) resolveInfo.loadLabel(packageManager);
                    if(appLabel.equals(Const.APP_NAME)) {
                        continue;
                    }
                    data.add(new AppModel(resolveInfo.activityInfo.packageName,
                            appLabel,
                            packageManager.getApplicationIcon(resolveInfo.activityInfo.applicationInfo)));
                }
            } catch (Exception e) {
                e.printStackTrace();
                emitter.tryOnError(e);
            }
            emitter.onSuccess(data);
        });
    }

    private Single<List<AppModel>> loadSavedFavoriteApps(List<FaveAppRecord> faveAppRecords) {
        return Single.fromCallable(() -> {
            List<AppModel> faveList = new ArrayList<>();
            // transform favorites retrieved from DB to appmodels that will be used in the UI
            for (FaveAppRecord faveAppRecord : faveAppRecords) {
                ApplicationInfo appInfo;
                try {
                    appInfo = packageManager.getApplicationInfo(faveAppRecord.getPackageName(), 0);
                    faveList.add(new AppModel(
                            appInfo.packageName,
                            packageManager.getApplicationLabel(appInfo).toString(),
                            packageManager.getApplicationIcon(appInfo)));
                } catch (PackageManager.NameNotFoundException e) {
                    // app was deleted/uninstalled, so delete record from DB
                    faveAppDao.deleteRecordByPackageName(faveAppRecord.getPackageName());
                    e.printStackTrace();
                }
            }
            return faveList;
        });
    }

    private List<ResolveInfo> getLaunchableActivities() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resInfos = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(resInfos, new ResolveInfo.DisplayNameComparator(packageManager));
        return resInfos;
    }

    /**
     * If fave apps are rearranged, overwrite existing fave apps list via this method.
     * This method basically calls an @Insert-annotated method with a REPLACE conflict strategy, which means
     * that if a similar record of a favorite app already exists in the DB, it will be replaced.
     * @param updatedList The list with rearranged favorite apps
     */
    public void updateList(List<AppModel> updatedList) {
        compositeDisposable.add(
            Single.fromCallable(() -> {
                List<FaveAppRecord> dbList = new ArrayList<>();
                for(AppModel appModel : updatedList) {
                    dbList.add(new FaveAppRecord(appModel.getPackageName(), appModel.getAppLabel()));
                }
                return dbList;
            })
            .flatMapCompletable(records -> Completable.fromAction(() -> faveAppDao.insert(records)))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> {}, Throwable::printStackTrace)
        );
    }

    public void clearFaveApps() {
        compositeDisposable.add(faveAppDao.clearFaveAppDb()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {}, Throwable::printStackTrace)
        );
    }
}
