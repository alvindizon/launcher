package com.alvindizon.launcher.features.applist;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AppListViewModel extends ViewModel {
    private static final String TAG = AppListViewModel.class.getSimpleName();

    private final PreferenceRepository preferenceRepository;
    private CompositeDisposable compositeDisposable;
    private PackageManager packageManager;
    private JsonAdapter<List<String>> faveAppJsonAdapter;
    private List<String> favePackageNameList = new ArrayList<>();


    @Inject
    public AppListViewModel(PreferenceRepository preferenceRepository, Moshi moshi) {
        this.preferenceRepository = preferenceRepository;
        compositeDisposable = new CompositeDisposable();
        faveAppJsonAdapter =  moshi.adapter(Types.newParameterizedType(List.class, String.class));
    }

    public void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    private List<ResolveInfo> getLaunchableActivities() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resInfos = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(resInfos, new ResolveInfo.DisplayNameComparator(packageManager));
        return resInfos;
    }

    public LiveData<List<AppModel>> getAppList() {
        MutableLiveData<List<AppModel>> appListData = new MutableLiveData<>();
        compositeDisposable.add(loadAppList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(appListData::setValue, Throwable::printStackTrace));
        return appListData;
    }

    public LiveData<SaveStatus> saveFaveAppList(List<AppModel> faveAppList) {
        Log.d(TAG, "saveFaveAppList: start");
        MutableLiveData<SaveStatus> saveStatus = new MutableLiveData<>();
        compositeDisposable.add(saveFaveAppListToPrefs(faveAppList)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(disposable -> saveStatus.setValue(SaveStatus.SAVING))
            .subscribe(() -> saveStatus.setValue(SaveStatus.DONE),
                    error -> {
                        Log.d(TAG, "saveFaveAppList: in livedata " + error.getMessage());
                        saveStatus.setValue(SaveStatus.ERROR);
                    })
        );
        return saveStatus;
    }

    private Single<List<AppModel>> loadAppList() {
        List<AppModel> data = new ArrayList<>();
        return Single.create(emitter -> {
            try {
                for(ResolveInfo resolveInfo : getLaunchableActivities()) {
                    String appLabel =(String) resolveInfo.loadLabel(packageManager);
                    if(appLabel.equals(Const.APP_NAME)) {
                        continue;
                    }
                    AppModel appModel = new AppModel();
                    appModel.setPackageName(resolveInfo.activityInfo.packageName);
                    appModel.setAppLabel(appLabel);
                    appModel.setLauncherIcon(packageManager.getApplicationIcon(resolveInfo.activityInfo.applicationInfo));
                    data.add(appModel);
                }
            } catch (Exception e) {
                e.printStackTrace();
                emitter.tryOnError(e);
            }

            emitter.onSuccess(data);
        });
    }

    private Completable saveFaveAppListToPrefs(List<AppModel> faveAppList) {
        return Completable.create(emitter -> {
            String faveAppListJsonString = preferenceRepository.get(R.string.key_fave_list, "");
            // get existing fave list if it exists
            if (!TextUtils.isEmpty(faveAppListJsonString)) {
                try {
                    favePackageNameList = faveAppJsonAdapter.fromJson(faveAppListJsonString);
                } catch (Exception e) {
                    Log.d(TAG, "saveFaveAppListToPrefs: get existing list");
                    e.printStackTrace();
                    emitter.tryOnError(e);
                }
            }
            for(AppModel appModel : faveAppList) {
                favePackageNameList.add(appModel.getPackageName());
            }
            Log.d(TAG, "favePackageNameList size: " + favePackageNameList.size());
            String faveAppListJson = "";
            try {
                faveAppListJson = faveAppJsonAdapter.toJson(favePackageNameList);
            } catch (Exception e) {
                Log.d(TAG, "saveFaveAppListToPrefs: convert list to string error");
                e.printStackTrace();
                emitter.tryOnError(e);
            }
            preferenceRepository.set(R.string.key_fave_list, faveAppListJson);
            emitter.onComplete();
        });
    }

}
