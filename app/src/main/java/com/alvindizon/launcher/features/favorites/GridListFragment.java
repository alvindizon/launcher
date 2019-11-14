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
import com.alvindizon.launcher.core.PreferenceHelper;
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

import static com.alvindizon.launcher.core.Const.GRID_SPAN_CNT;

public class GridListFragment extends Fragment {
    private static final String TAG = GridListFragment.class.getSimpleName();

    @Inject
    public ViewModelFactory viewModelFactory;

    @Inject
    public PreferenceHelper preferenceHelper;

    private FragmentGridListBinding binding;
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
                if(viewModel.getFaveList().contains(appModel)) {
                    viewModel.reorderAppList(appModel, finalPosition);
                    favoritesAdapter.notifyItemMoved(initialPosition, finalPosition);
                    favoritesAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void onSwipe(AppModel appModel, int position) {
        if(viewModel.getFaveList().contains(appModel)) {
            Log.d(TAG, "onSwipe: " + appModel.getAppLabel() + " removed!");
            viewModel.deleteRecord(appModel);
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGridListBinding.inflate(inflater, container, false);
        navController = ((MainActivity) requireActivity()).getNavController();
        setHasOptionsMenu(true);
        favoritesAdapter = new FavoritesAdapter(new ArrayList<>(), this::launchApp);
        binding.rv.setAdapter(favoritesAdapter);
        binding.rv.setSwipeListener(onItemSwipeListener);
        binding.rv.setDragListener(onItemDragListener);

        // setup layout manager
        binding.rv.setLayoutManager(new GridLayoutManager(requireContext(), GRID_SPAN_CNT));

        // setup rv orientation
        binding.rv.setOrientation(DragDropSwipeRecyclerView.ListOrientation.GRID_LIST_WITH_HORIZONTAL_SWIPING);

        // set number of columns
        binding.rv.setNumOfColumnsPerRowInGridList(3);

        // trigger dragging action on long press
        binding.rv.setLongPressToStartDragging(true);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
            new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // do nothing
                }
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
                favoritesAdapter.notifyDataSetChanged();
                viewModel.clearFaveApps();
                updateRecyclerView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // load favorite apps here so that list is refreshed whenever an app is uninstalled
        viewModel.loadFaveAppList().observe(getViewLifecycleOwner(), list -> {
            for(AppModel appModel : list) {
                Log.d(TAG, appModel.toString());
            }
            viewModel.setFaveList(list);
            favoritesAdapter.setDataSet(list);
            updateRecyclerView();
        });
    }

    @Override
    public void onStop() {
        preferenceHelper.set(R.string.key_is_list, false);
        viewModel.saveFaveApps().observe(getViewLifecycleOwner(), status -> handleSaveStatus(status));
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
        if(viewModel.getFaveList().isEmpty()) {
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
