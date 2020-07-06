package com.alvindizon.launcher.features.favorites;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.core.applist.AppModel;
import com.alvindizon.launcher.core.applist.AppModelDiffCallback;
import com.alvindizon.launcher.core.ui.LauncherIcons;
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter;

import java.util.List;

public class FavoritesAdapter extends DragDropSwipeAdapter<AppModel, FavoritesAdapter.ViewHolder> {

    public interface FaveItemClickListener {
        void onItemClick(String packageName);
    }

    private FaveItemClickListener onFaveItemClickListener;
    private List<AppModel> appList;

    class ViewHolder extends DragDropSwipeAdapter.ViewHolder {
        private final ImageView appIcon;
        private final TextView appLabel;

        public ViewHolder(View layout) {
            super(layout);
            this.appIcon = layout.findViewById(R.id.app_icon);
            this.appLabel = layout.findViewById(R.id.app_label);
        }

        private void bind(AppModel appModel, int position) {
            Bitmap bitmap = LauncherIcons.createIconBitmap(appModel.getLauncherIcon(), itemView.getContext(), 1f);
            appIcon.setImageBitmap(bitmap);
            appLabel.setText(appModel.getAppLabel());

            this.itemView.setOnClickListener(v ->
                    onFaveItemClickListener.onItemClick(appList.get(position).getPackageName()));
        }
    }

    public FavoritesAdapter(FaveItemClickListener onFaveItemClickListener) {
//        setDataSet(list);
        this.onFaveItemClickListener = onFaveItemClickListener;
    }

    @Override
    protected ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    protected View getViewToTouchToStartDraggingItem(AppModel appModel, ViewHolder viewHolder, int i) {
        // return null so that whole item layout is used as a drag handle
        return null;
    }

    @Override
    protected void onBindViewHolder(AppModel appModel, ViewHolder viewHolder, int i) {
        viewHolder.bind(appModel, i);
    }

    public void setAppList(List<AppModel> appList) {
        this.appList = appList;
        setDataSet(appList);
    }

    public void updateList(List<AppModel> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new AppModelDiffCallback(appList, newList));
        diffResult.dispatchUpdatesTo(this);
    }
}
