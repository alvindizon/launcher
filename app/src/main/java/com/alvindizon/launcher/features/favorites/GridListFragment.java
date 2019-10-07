package com.alvindizon.launcher.features.favorites;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.application.MainActivity;
import com.alvindizon.launcher.application.MainViewModel;
import com.alvindizon.launcher.core.AppModel;
import com.alvindizon.launcher.core.PreferenceRepository;
import com.alvindizon.launcher.core.SaveStatus;
import com.alvindizon.launcher.core.ViewModelFactory;
import com.alvindizon.launcher.databinding.FragmentGridListBinding;
import com.alvindizon.launcher.di.Injector;
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView;
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener;
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GridListFragment extends Fragment {
    private static final String TAG = GridListFragment.class.getSimpleName();

    @Inject
    public ViewModelFactory viewModelFactory;

    @Inject
    public PreferenceRepository preferenceRepository;

    private FragmentGridListBinding binding;
    private List<AppModel> faveList = new ArrayList<>();
    private NavController navController;
    private PackageManager packageManager;
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
            Log.d(TAG, appModel.getAppLabel() + " is being moved from position " + previousPosition + " to " + newPosition);
        }

        @Override
        public void onItemDropped(int initialPosition, int finalPosition, AppModel appModel) {
            if(initialPosition != finalPosition) {
                Log.d(TAG, appModel.getAppLabel() + " moved from position " + initialPosition + " to " + finalPosition);
                if(faveList.contains(appModel)) {
                    faveList.remove(appModel);
                    faveList.add(finalPosition, appModel);
                    viewModel.saveFaveApps(faveList).observe(getViewLifecycleOwner(), status -> handleSaveStatus(status));
                    favoritesAdapter.notifyItemMoved(initialPosition, finalPosition);
                    favoritesAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void onSwipe(AppModel appModel, int position) {
        if(faveList.contains(appModel)) {
            Log.d(TAG, "onSwipe: " + appModel.getAppLabel() + " removed!");
            faveList.remove(appModel);
            viewModel.saveFaveApps(faveList).observe(getViewLifecycleOwner(), this::handleSaveStatus);
            favoritesAdapter.notifyDataSetChanged();
            updateRecyclerView();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Injector.getViewModelComponent().inject(this);
        viewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(MainViewModel.class);
        packageManager = requireActivity().getPackageManager();
        viewModel.setPackageManager(packageManager);
        navController = ((MainActivity) requireActivity()).getNavController();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGridListBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        favoritesAdapter = new FavoritesAdapter(faveList, this::launchApp);
        binding.rv.setAdapter(favoritesAdapter);
        binding.rv.setSwipeListener(onItemSwipeListener);
        binding.rv.setDragListener(onItemDragListener);

        // setup layout manager
        binding.rv.setLayoutManager(new GridLayoutManager(requireContext(), 4));

        // setup rv orientation
        binding.rv.setOrientation(DragDropSwipeRecyclerView.ListOrientation.GRID_LIST_WITH_HORIZONTAL_SWIPING);

        // set number of columns
        binding.rv.setNumOfColumnsPerRowInGridList(3);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
            new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // do nothing
                }
        });

        viewModel.loadFaveAppList().observe(getViewLifecycleOwner(), list -> {
            faveList = list;
            favoritesAdapter.setDataSet(list);
            updateRecyclerView();
        });

        binding.fab.setOnClickListener(v -> navController.navigate(R.id.action_grid_list_dest_to_app_list_dest));

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favorite, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        if(this.getClass() == GridListFragment.class) {
            menu.findItem(R.id.menu_switch_to_grid).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_switch_to_vertical:
                navController.navigate(R.id.action_grid_list_dest_to_vertical_list_dest);
                return true;
            case R.id.menu_clear_apps:
                faveList.clear();
                favoritesAdapter.notifyDataSetChanged();
                viewModel.saveFaveApps(faveList).observe(getViewLifecycleOwner(), this::handleSaveStatus);
                updateRecyclerView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        preferenceRepository.set(R.string.key_is_list, false);
        super.onStop();
    }

    private void handleSaveStatus(SaveStatus saveStatus) {
        switch (saveStatus) {
            case SAVING:
                Log.d(TAG, "handleSaveStatus: saving");
                break;
            case DONE:
                Log.d(TAG, "handleSaveStatus: done");
                break;
            case ERROR:
                Log.d(TAG, "handleSaveStatus: error");
                Toast.makeText(requireContext(), R.string.prompt_error, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
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
}
