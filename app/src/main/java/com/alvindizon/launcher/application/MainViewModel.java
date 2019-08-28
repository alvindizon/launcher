package com.alvindizon.launcher.application;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.core.AppModel;
import com.alvindizon.launcher.core.Const;
import com.alvindizon.launcher.core.PreferenceRepository;
import com.alvindizon.launcher.core.SaveStatus;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
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

    private final PreferenceRepository preferenceRepository;
    private JsonAdapter<List<String>> jsonAdapter;
    private List<String> favePackageNameList = new ArrayList<>();
    private PackageManager packageManager;
    private CompositeDisposable compositeDisposable;

    @Inject
    public MainViewModel(PreferenceRepository preferenceRepository, Moshi moshi) {
        this.preferenceRepository = preferenceRepository;
        this.compositeDisposable = new CompositeDisposable();
        jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, String.class));
    }

    public void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    public LiveData<List<AppModel>> getLaunchableApps() {
        MutableLiveData<List<AppModel>> appListData = new MutableLiveData<>();
        compositeDisposable.add(getLaunchableList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(appListData::setValue, Throwable::printStackTrace));
        return appListData;
    }

    public LiveData<SaveStatus> saveFaveApps(List<AppModel> faveList) {
        MutableLiveData<SaveStatus> saveStatus = new MutableLiveData<>();
        favePackageNameList.clear();
        compositeDisposable.add(saveFaveAppListToPrefs(faveList)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(disposable -> saveStatus.setValue(SaveStatus.SAVING))
            .subscribe(() -> saveStatus.setValue(SaveStatus.DONE),
                error -> {
                    error.printStackTrace();
                    saveStatus.setValue(SaveStatus.ERROR);
                })
        );
        return saveStatus;
    }

    public LiveData<SaveStatus> saveToExistingFaves(List<AppModel> faveList) {
        MutableLiveData<SaveStatus> saveStatus = new MutableLiveData<>();
        compositeDisposable.add(saveToExistingList(faveList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> saveStatus.setValue(SaveStatus.SAVING))
                .subscribe(() -> saveStatus.setValue(SaveStatus.DONE),
                        error -> {
                            error.printStackTrace();
                            saveStatus.setValue(SaveStatus.ERROR);
                        })
        );
        return saveStatus;
    }

    public LiveData<List<AppModel>> loadFaveAppList() {
        MutableLiveData<List<AppModel>> listData = new MutableLiveData<>();
        compositeDisposable.add(loadSavedFavoriteApps()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listData::setValue, Throwable::printStackTrace));
        return listData;
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

    private Completable saveToExistingList(List<AppModel> faveAppList) {
        return Completable.create(emitter -> {
            String faveAppListJsonString = preferenceRepository.get(R.string.key_fave_list, "");
            // get existing fave list if it exists
            if (!TextUtils.isEmpty(faveAppListJsonString)) {
                try {
                    favePackageNameList = jsonAdapter.fromJson(faveAppListJsonString);
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.tryOnError(e);
                }
            }
            emitter.onComplete();
        }).andThen(saveFaveAppListToPrefs(faveAppList));
    }

    private Completable saveFaveAppListToPrefs(List<AppModel> faveAppList) {
        return Completable.create(emitter -> {

            for(AppModel appModel : faveAppList) {
                favePackageNameList.add(appModel.getPackageName());
            }
            String faveAppListJson = "";
            try {
                faveAppListJson = jsonAdapter.toJson(favePackageNameList);
            } catch (Exception e) {
                e.printStackTrace();
                emitter.tryOnError(e);
            }
            preferenceRepository.set(R.string.key_fave_list, faveAppListJson);
            emitter.onComplete();
        });
    }

    private Single<List<AppModel>> loadSavedFavoriteApps() {
        return Single.create(emitter -> {
            String faveAppListJsonString = preferenceRepository.get(R.string.key_fave_list, "");
            // use new list every time app list is loaded from prefs to prevent duplicates
            List<AppModel> faveList = new ArrayList<>();
            if (!TextUtils.isEmpty(faveAppListJsonString)) {
                try {
                    favePackageNameList = jsonAdapter.fromJson(faveAppListJsonString);
                } catch (IOException e) {
                    e.printStackTrace();
                    emitter.tryOnError(e);
                }
            }
            // transform favePackageNameList to appmodels
            if (favePackageNameList != null && !favePackageNameList.isEmpty()) {
                for (String packageName : favePackageNameList) {
                    ApplicationInfo appInfo = null;
                    try {
                        appInfo = packageManager.getApplicationInfo(packageName, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        emitter.tryOnError(e);
                    }
                    faveList.add(new AppModel(appInfo.packageName,
                            packageManager.getApplicationLabel(appInfo).toString(),
                            packageManager.getApplicationIcon(appInfo)));
                }
                emitter.onSuccess(faveList);
            }  else {
                emitter.tryOnError(new Exception("empty list"));
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
