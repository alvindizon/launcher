package com.alvindizon.launcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    public interface AppItemListener {
        void onItemClick(String packageName);
    }

    private List<AppModel> appList = new ArrayList<>();
    private AppItemListener onAppItemClickListener;

    public AppListAdapter(AppItemListener onAppItemClickListener) {
        this.onAppItemClickListener = onAppItemClickListener;
    }

    public void setAppList(List<AppModel> appList) {
        this.appList = appList;
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView appIcon;
        private final TextView appLabel;
        private final TextView appPackageName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appLabel = itemView.findViewById(R.id.app_label);
            appPackageName = itemView.findViewById(R.id.app_package);
        }

        private void bind(AppModel app, int i) {
            appIcon.setImageDrawable(app.getLauncherIcon());
            appLabel.setText(app.getAppLabel());
            appPackageName.setText(app.getPackageName());
            this.itemView.setOnClickListener(v -> onAppItemClickListener.onItemClick(appList.get(i).getPackageName()));
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
