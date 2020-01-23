package com.alvindizon.launcher.features.favorites;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.core.AppModel;
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter;

import java.util.List;

public class FavoritesAdapter extends DragDropSwipeAdapter<AppModel, FavoritesAdapter.ViewHolder> {

    public interface FaveItemClickListener {
        void onItemClick(String packageName);
    }

    private FaveItemClickListener onFaveItemClickListener;

    class ViewHolder extends DragDropSwipeAdapter.ViewHolder {
        private final ImageView appIcon;
        private final TextView appLabel;

        public ViewHolder(View layout) {
            super(layout);
            this.appIcon = layout.findViewById(R.id.app_icon);
            this.appLabel = layout.findViewById(R.id.app_label);
        }

        private void bind(AppModel appModel, int position) {
            appIcon.setImageDrawable(appModel.getLauncherIcon());
            appLabel.setText(appModel.getAppLabel());

            this.itemView.setOnClickListener(v ->
                    onFaveItemClickListener.onItemClick(getDataSet().get(position).getPackageName()));
        }
    }

    public FavoritesAdapter(List<AppModel> list, FaveItemClickListener onFaveItemClickListener) {
        setDataSet(list);
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
}
