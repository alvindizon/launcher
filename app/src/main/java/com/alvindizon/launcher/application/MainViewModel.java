package com.alvindizon.launcher.application;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alvindizon.launcher.core.AppModel;
import com.alvindizon.launcher.core.Const;
import com.alvindizon.launcher.core.SaveStatus;
import com.alvindizon.launcher.data.FaveAppRecord;
import com.alvindizon.launcher.data.FaveAppRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();

    private final FaveAppRepository faveAppRepository;

    private PackageManager packageManager;
    private CompositeDisposable compositeDisposable;
    private List<AppModel> faveList = new ArrayList<>();

    @Inject
    public MainViewModel(FaveAppRepository faveAppRepository) {
        this.faveAppRepository = faveAppRepository;
        this.compositeDisposable = new CompositeDisposable();
    }

    public void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    public void setFaveList(List<AppModel> newList) {
        faveList = newList;
    }

    public void reorderAppList(AppModel appModel, int position) {
        faveList.remove(appModel);
        faveList.add(position, appModel);
    }

    public List<AppModel> getFaveList() {
        return faveList;
    }

    public LiveData<List<AppModel>> getLaunchableApps() {
        MutableLiveData<List<AppModel>> appListData = new MutableLiveData<>();
        compositeDisposable.add(getLaunchableList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(appListData::setValue, Throwable::printStackTrace));
        return appListData;
    }

    public LiveData<SaveStatus> saveFaveApps() {
        MutableLiveData<SaveStatus> saveStatus = new MutableLiveData<>();
        compositeDisposable.add(
            faveAppRepository.clearFaveAppDb()
            .andThen(Observable.fromIterable(faveList)
                .flatMapCompletable(appModel -> faveAppRepository.createRecordCompletable(
                        new FaveAppRecord(appModel.getPackageName(), appModel.getAppLabel())
                    )
                )
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(disposable -> saveStatus.setValue(SaveStatus.SAVING))
            .subscribe(() -> saveStatus.setValue(SaveStatus.DONE),
                error -> {
                    error.printStackTrace();
                    saveStatus.setValue(SaveStatus.ERROR);
                }
            )
        );
        return saveStatus;
    }

    public LiveData<List<AppModel>> loadFaveAppList() {
        MutableLiveData<List<AppModel>> listData = new MutableLiveData<>();
        compositeDisposable.add(faveAppRepository.getFaveApps()
            .defaultIfEmpty(new ArrayList<>())
            .flatMapSingle(this::loadSavedFavoriteApps)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(listData::setValue, Throwable::printStackTrace));
        return listData;
    }

    public void deleteRecord(AppModel appModel) {
        compositeDisposable.add(faveAppRepository.deleteRecordByName(appModel.getPackageName())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> faveList.remove(appModel), Throwable::printStackTrace)
        );
    }

    public void clearFaveApps() {
        compositeDisposable.add(faveAppRepository.clearFaveAppDb()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> {
                faveList.clear();
            }, Throwable::printStackTrace)
        );
    }

    public void addFaveApp(AppModel app) {
        compositeDisposable.add(faveAppRepository.createRecordCompletable(
                new FaveAppRecord(app.getPackageName(), app.getAppLabel())
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> Log.d(TAG, "addFaveApp: success"), Throwable::printStackTrace)
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
        return Single.create(emitter -> {
            List<AppModel> faveList = new ArrayList<>();
            if(!faveAppRecords.isEmpty()) {
                // transform favorites retrieved from DB to appmodels that will be used in the UI
                for (FaveAppRecord faveAppRecord : faveAppRecords) {
                    ApplicationInfo appInfo = null;
                    try {
                        appInfo = packageManager.getApplicationInfo(faveAppRecord.getPackageName(), 0);
                        faveList.add(new AppModel(appInfo.packageName,
                                packageManager.getApplicationLabel(appInfo).toString(),
                                packageManager.getApplicationIcon(appInfo)));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                emitter.onSuccess(faveList);
            } else {
                emitter.onSuccess(new ArrayList<>());
            }
        });
    }
    private List<ResolveInfo> getLaunchableActivities() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resInfos = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(resInfos, new ResolveInfo.DisplayNameComparator(packageManager));
        return resInfos;
    }

}
