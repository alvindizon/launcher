package com.alvindizon.launcher.features.favorites;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.core.PreferenceHelper;
import com.alvindizon.launcher.core.applist.AppModel;
import com.alvindizon.launcher.databinding.FragmentFaveListBinding;
import com.alvindizon.launcher.di.Injector;
import com.alvindizon.launcher.di.module.ActivityModule;
import com.alvindizon.launcher.features.main.MainViewModel;
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView;
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener;
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class FavoritesFragment extends Fragment {

    @Inject
    PreferenceHelper preferenceHelper;

    @Inject
    PackageManager packageManager;

    private FragmentFaveListBinding binding;

    private List<AppModel> faveList = new ArrayList<>();

    private MainViewModel viewModel;

    private FavoritesAdapter favoritesAdapter;

    private OnItemSwipeListener<AppModel> onItemSwipeListener = (position, swipeDirection, appModel) -> {
        switch (swipeDirection) {
            case RIGHT_TO_LEFT:
            case LEFT_TO_RIGHT:
                onSwipe(appModel, position);
                break;
            default:
                break;
        }
        return false;
    };

    private OnItemDragListener<AppModel> onItemDragListener = new OnItemDragListener<AppModel>() {
        @Override
        public void onItemDragged(int previousPosition, int newPosition, AppModel appModel) {
        }

        @Override
        public void onItemDropped(int initialPosition, int finalPosition, AppModel appModel) {
            if(initialPosition != finalPosition) {
                if(faveList.contains(appModel)) {
                    faveList.remove(appModel);
                    faveList.add(finalPosition, appModel);
                    favoritesAdapter.updateList(faveList);
                    viewModel.updateList(faveList);
                }
            }
        }
    };

    private void onSwipe(AppModel appModel, int position) {
        if(faveList.contains(appModel)) {
            faveList.remove(appModel);
            favoritesAdapter.updateList(faveList);
            viewModel.deleteFaveApp(appModel);
            updateRecyclerView();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Injector.get().activityComponent(new ActivityModule(requireActivity())).inject(this);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFaveListBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);

        setupRecyclerView();

        setupGridSpanCount();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
            new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // do nothing
                }
        });

        binding.fab.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_fave_list_dest_to_app_list_dest));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getListData().observe(getViewLifecycleOwner(), list -> {
            faveList = list;
            favoritesAdapter.setAppList(list);
            updateRecyclerView();
        });
    }

    private void setupRecyclerView() {
        favoritesAdapter = new FavoritesAdapter(this::launchApp);
        favoritesAdapter.setAppList(faveList);
        binding.rv.setAdapter(favoritesAdapter);
        binding.rv.setSwipeListener(onItemSwipeListener);
        binding.rv.setDragListener(onItemDragListener);

        // trigger dragging action on long press
        binding.rv.setLongPressToStartDragging(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favorite, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear_apps:
                faveList.clear();
                viewModel.clearFaveApps();
                updateRecyclerView();
                return true;
            case R.id.menu_column_count:
                chooseColumnCount();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void chooseColumnCount() {
        String[] columnSpanCount = {"1", "2", "3"};
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.label_column_count)
                .setSingleChoiceItems(columnSpanCount, 0, (dialog, which) -> {
                    preferenceHelper.set(R.string.key_column_cnt, columnSpanCount[which]);
                    setupRecyclerView();
                    setupGridSpanCount();
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadFaveAppList();

        setupGridSpanCount();
    }


    private void updateRecyclerView() {
        binding.loadingIndicator.setVisibility(View.GONE);
        if(faveList.isEmpty()) {
            binding.emptyMsg.setVisibility(View.VISIBLE);
            binding.rv.setVisibility(View.GONE);
        } else {
            binding.emptyMsg.setVisibility(View.GONE);
            binding.rv.setVisibility(View.VISIBLE);
        }
    }

    private void launchApp(String packageName) {
        try {
            startActivity(packageManager.getLaunchIntentForPackage(packageName));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Could not launch app \" " + packageName + "\"", Toast.LENGTH_LONG).show();
        }
    }

    private void setupGridSpanCount() {
        int gridSpanCnt = Integer.parseInt(preferenceHelper.get(R.string.key_column_cnt, getString(R.string.pref_default_grid_span_cnt)));

        // setup layout manager, orientation, and swiping
        if(gridSpanCnt == 1) {
            binding.rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rv.setOrientation(DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING);
            binding.rv.setItemLayoutId(R.layout.item_fave);
        } else {
            binding.rv.setLayoutManager(new GridLayoutManager(requireContext(), gridSpanCnt));
            binding.rv.setOrientation(DragDropSwipeRecyclerView.ListOrientation.GRID_LIST_WITH_HORIZONTAL_SWIPING);
            if(gridSpanCnt == 2) {
                binding.rv.setItemLayoutId(R.layout.item_fave_grid_large);
            } else {
                binding.rv.setItemLayoutId(R.layout.item_fave_grid);
            }
        }

        // set number of columns (will be ignored if vertical list)
        binding.rv.setNumOfColumnsPerRowInGridList(gridSpanCnt);
    }

}
