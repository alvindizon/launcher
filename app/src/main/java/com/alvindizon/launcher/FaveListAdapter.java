package com.alvindizon.launcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FaveListAdapter extends RecyclerView.Adapter<FaveListAdapter.ViewHolder> {
    public interface FaveItemClickListener {
        void onItemClick(String packageName);
    }

    private List<AppModel> appList = new ArrayList<>();
    private FaveItemClickListener onFaveItemClickListener;

    public FaveListAdapter(FaveItemClickListener onFaveItemClickListener) {
        this.onFaveItemClickListener = onFaveItemClickListener;
    }

    public void setAppList(List<AppModel> appList) {
        this.appList = appList;
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView appIcon;
        private final TextView appLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appLabel = itemView.findViewById(R.id.app_label);
        }

        private void bind(AppModel app, int i) {
            appIcon.setImageDrawable(app.getLauncherIcon());
            appLabel.setText(app.getAppLabel());
            this.itemView.setOnClickListener(v -> onFaveItemClickListener.onItemClick(appList.get(i).getPackageName()));
        }
    }

    @NonNull
    @Override
    public FaveListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fave, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FaveListAdapter.ViewHolder holder, int position) {
        holder.bind(appList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }
}
