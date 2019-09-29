package com.alvindizon.launcher.features.applist;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.core.AppModel;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    public static final String TAG = AppListAdapter.class.getSimpleName();

    public interface AppItemListener {
        void onItemClick(AppModel app);
        void onItemUncheck(AppModel app);
    }

    private List<AppModel> appList;
    private AppItemListener onAppItemClickListener;
    private SparseBooleanArray isCheckedArray = new SparseBooleanArray();

    public AppListAdapter(AppItemListener onAppItemClickListener) {
        this.onAppItemClickListener = onAppItemClickListener;
    }

    public void setAppList(List<AppModel> appList) {
        this.appList = appList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView appIcon;
        private final TextView appLabel;
        private final CheckBox appCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appLabel = itemView.findViewById(R.id.app_label);
            appCheckBox = itemView.findViewById(R.id.check_box);
        }

        private void bind(AppModel app, int i) {
            appIcon.setImageDrawable(app.getLauncherIcon());
            appLabel.setText(app.getAppLabel());
            appCheckBox.setChecked(isCheckedArray.get(i, false));
            appCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isCheckedArray.put(i, isChecked);
                if(isChecked) {
                    Log.d(TAG, "isChecked: " + app.getPackageName());
                    onAppItemClickListener.onItemClick(app);
                } else {
                    Log.d(TAG, "!isChecked: " + app.getPackageName());
                    onAppItemClickListener.onItemUncheck(app);
                }
            });
        }
    }

    @NonNull
    @Override
    public AppListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppListAdapter.ViewHolder holder, int position) {
        holder.bind(appList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }
}

