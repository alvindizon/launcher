package com.alvindizon.launcher.features.applist;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.core.AppModel;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    public interface MultiSelectItemListener {
        void onActionBarButtonClick(List<AppModel> newAppList);
    }

    private List<AppModel> appList = new ArrayList<>();
    private MultiSelectItemListener multiSelectItemListener;
    private boolean multiSelect = false;
    private List<AppModel> selectedItems = new ArrayList<>();
    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            multiSelect = true;
            menu.add("Add");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            appList.addAll(selectedItems);
            if(multiSelectItemListener != null) {
                multiSelectItemListener.onActionBarButtonClick(appList);
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelect = false;
            selectedItems.clear();
            notifyDataSetChanged();
        }
    };

    public void setAppList(List<AppModel> appList) {
        this.appList = appList;
        notifyDataSetChanged();
    }

    public void setMultiSelectItemListener(MultiSelectItemListener multiSelectItemListener) {
        this.multiSelectItemListener = multiSelectItemListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView appIcon;
        private final TextView appLabel;
        private final ConstraintLayout itemLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appLabel = itemView.findViewById(R.id.app_label);
            itemLayout = itemView.findViewById(R.id.item_layout);
        }

        private void selectItem(AppModel app) {
            if(multiSelect) {
                if(selectedItems.contains(app)) {
                    selectedItems.remove(app);
                    itemLayout.setBackgroundColor(Color.WHITE);
                } else {
                    selectedItems.add(app);
                    itemLayout.setBackgroundColor(Color.LTGRAY);
                }
            }
        }

        private void bind(AppModel app, int i) {
            appIcon.setImageDrawable(app.getLauncherIcon());
            appLabel.setText(app.getAppLabel());

            if(selectedItems.contains(app)) {
                itemLayout.setBackgroundColor(Color.LTGRAY);
            } else {
                itemLayout.setBackgroundColor(Color.WHITE);
            }

            this.itemView.setOnClickListener(v ->{
                if(multiSelect) {
                    selectItem(app);
                }
            });
            this.itemView.setOnLongClickListener(v -> {
                ((AppCompatActivity) v.getContext()).startSupportActionMode(actionModeCallbacks);
                selectItem(app);
                return true;
            });
        }
    }

    @NonNull
    @Override
    public AppListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fave, parent, false);

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
