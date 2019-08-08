package com.alvindizon.launcher.features.faveapps;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.core.AppModel;
import com.alvindizon.launcher.core.PreferenceRepository;
import com.alvindizon.launcher.core.SaveStatus;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FavoritesViewModel extends ViewModel {
    private static final String TAG = FavoritesViewModel.class.getSimpleName();

    private final PreferenceRepository preferenceRepository;

    private PackageManager packageManager;
    private CompositeDisposable compositeDisposable;
    private JsonAdapter<List<String>> faveAppJsonAdapter;
    private List<AppModel> faveList = new ArrayList<>();
    private List<String> favePackageNameList = new ArrayList<>();

    @Inject
    public FavoritesViewModel(PreferenceRepository preferenceRepository, Moshi moshi) {
        this.preferenceRepository = preferenceRepository;
        compositeDisposable = new CompositeDisposable();
        faveAppJsonAdapter =  moshi.adapter(Types.newParameterizedType(List.class, String.class));
    }

    public void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    public LiveData<List<AppModel>> loadFaveAppList() {
        Log.d(TAG, "loadFaveAppList: start");
        MutableLiveData<List<AppModel>> listData = new MutableLiveData<>();
        compositeDisposable.add(loadFavesFromPrefs()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(listData::setValue, Throwable::printStackTrace));
        return listData;
    }

    public LiveData<SaveStatus> saveFaveApps(List<AppModel> faveList) {
        Log.d(TAG, "saveFaveApps: start");
        MutableLiveData<SaveStatus> saveStatus = new MutableLiveData<>();
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

    private Single<List<AppModel>> loadFavesFromPrefs() {
        return Single.create(emitter -> {
            String faveAppListJsonString = preferenceRepository.get(R.string.key_fave_list, "");
            if (!TextUtils.isEmpty(faveAppListJsonString)) {
                try {
                    favePackageNameList = faveAppJsonAdapter.fromJson(faveAppListJsonString);
                } catch (IOException e) {
                    e.printStackTrace();
                    emitter.tryOnError(e);
                }
            }
            // transform favePackageNameList to appmodels
            if (favePackageNameList != null && !favePackageNameList.isEmpty()) {
                for (String packageName : favePackageNameList) {
                    AppModel appModel = new AppModel();
                    ApplicationInfo appInfo = null;
                    try {
                        appInfo = packageManager.getApplicationInfo(packageName, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        emitter.tryOnError(e);
                    }
                    appModel.setPackageName(appInfo.packageName);
                    appModel.setLauncherIcon(packageManager.getApplicationIcon(appInfo));
                    appModel.setAppLabel(packageManager.getApplicationLabel(appInfo).toString());
                    faveList.add(appModel);
                }
                emitter.onSuccess(faveList);
            }  else {
                emitter.tryOnError(new Exception("empty list"));
            }
        });
    }

    private Completable saveFaveAppListToPrefs(List<AppModel> faveAppList) {
        return Completable.create(emitter -> {
            List<String> favePackageNameList = new ArrayList<>();
            for(AppModel appModel : faveAppList) {
                favePackageNameList.add(appModel.getPackageName());
            }
            String faveAppListJson = "";
            Log.d(TAG, "saveFaveAppListToPrefs: list to be saved is " + favePackageNameList.size());
            try {
                faveAppListJson = faveAppJsonAdapter.toJson(favePackageNameList);
            } catch (Exception e) {
                e.printStackTrace();
                emitter.tryOnError(e);
            }

            preferenceRepository.set(R.string.key_fave_list, faveAppListJson);
            Log.d(TAG, "saveFaveAppListToPrefs: done");
            emitter.onComplete();
        });
    }
}
