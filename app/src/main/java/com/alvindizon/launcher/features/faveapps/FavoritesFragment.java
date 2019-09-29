package com.alvindizon.launcher.features.faveapps;

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
import androidx.recyclerview.widget.RecyclerView;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.application.MainActivity;
import com.alvindizon.launcher.application.MainViewModel;
import com.alvindizon.launcher.core.AppModel;
import com.alvindizon.launcher.core.PreferenceRepository;
import com.alvindizon.launcher.core.SaveStatus;
import com.alvindizon.launcher.core.ViewModelFactory;
import com.alvindizon.launcher.databinding.FragmentFavoritesBinding;
import com.alvindizon.launcher.di.Injector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FavoritesFragment extends Fragment {
    public static final String TAG = FavoritesFragment.class.getSimpleName();

    private PackageManager packageManager;
    private FaveListAdapter faveListAdapter;
    FragmentFavoritesBinding binding;
    private List<AppModel> faveList = new ArrayList<>();
    private NavController navController;

    @Inject
    public ViewModelFactory viewModelFactory;

    @Inject
    public PreferenceRepository preferenceRepository;

    private MainViewModel viewModel;
    private GridLayoutManager layoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Injector.getViewModelComponent().inject(this);
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(MainViewModel.class);
        packageManager = requireActivity().getPackageManager();
        viewModel.setPackageManager(packageManager);
        navController = ((MainActivity) requireActivity()).getNavController();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true); // set to true in order for options menu to be inflated
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
            new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // do nothing on back press
                }
            });

        int spanCount = preferenceRepository.get(R.string.key_span_count, 1);
        layoutManager = new GridLayoutManager(requireContext(), spanCount);
        binding.rvNav.setLayoutManager(layoutManager);
        faveListAdapter = new FaveListAdapter(this::launchApp, layoutManager);
        binding.rvNav.setAdapter(faveListAdapter);
        faveListAdapter.setAppList(faveList);

        faveListAdapter.setOnDeleteItemListener(list -> {
            faveList = list;
            viewModel.saveFaveApps(faveList).observe(getViewLifecycleOwner(), this::handleSaveStatus);
            updateRecyclerView();
        });

        viewModel.loadFaveAppList().observe(getViewLifecycleOwner(), list -> {
            faveList = list;
            faveListAdapter.swapItems(list);
            updateRecyclerView();
        });

        binding.rvNav.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.fab.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(dy < 0 || dy > 0 && binding.fab.isShown()) {
                    binding.fab.hide();
                }
            }
        });

        binding.fab.setOnClickListener(v -> navController.navigate(R.id.action_favorites_dest_to_app_list_dest));

        return binding.getRoot();
    }

    private void updateRecyclerView() {
        binding.progressBar.setVisibility(View.GONE);
        if(faveList.isEmpty()) {
            binding.emptyMsg.setVisibility(View.VISIBLE);
            binding.rvNav.setVisibility(View.GONE);
        } else {
            binding.emptyMsg.setVisibility(View.GONE);
            binding.rvNav.setVisibility(View.VISIBLE);
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favorite, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_switch_view:
                if (layoutManager.getSpanCount() == 1) {
                    layoutManager.setSpanCount(3);
                } else {
                    layoutManager.setSpanCount(1);
                }
                faveListAdapter.notifyItemRangeChanged(0, (faveListAdapter != null ? faveListAdapter.getItemCount() : 0));
                return true;
            case R.id.menu_clear_apps:
                faveList.clear();
                faveListAdapter.notifyDataSetChanged();
                viewModel.saveFaveApps(faveList).observe(getViewLifecycleOwner(), this::handleSaveStatus);
                updateRecyclerView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(faveListAdapter != null) {
            layoutManager.setSpanCount(preferenceRepository.get(R.string.key_span_count, 1));
            faveListAdapter.notifyItemRangeChanged(0, faveListAdapter.getItemCount());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        preferenceRepository.set(R.string.key_span_count, layoutManager.getSpanCount());
    }
}
