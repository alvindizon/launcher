package com.alvindizon.launcher.core.applist;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class AppModelDiffCallback extends DiffUtil.Callback {

    private List<AppModel> oldList;
    private List<AppModel> newList;

    public AppModelDiffCallback(List<AppModel> oldList, List<AppModel> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getPackageName().equals(newList.get(newItemPosition).getPackageName());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        AppModel oldApp = oldList.get(oldItemPosition);
        AppModel newApp = newList.get(newItemPosition);

        return oldApp.getPackageName().equals(newApp.getPackageName()) &&
                oldApp.getAppLabel().equals(newApp.getAppLabel());
    }
}
