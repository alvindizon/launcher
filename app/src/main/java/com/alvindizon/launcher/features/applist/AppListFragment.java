package com.alvindizon.launcher.features.applist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.application.MainActivity;
import com.alvindizon.launcher.application.MainViewModel;
import com.alvindizon.launcher.core.AppModel;
import com.alvindizon.launcher.core.SaveStatus;
import com.alvindizon.launcher.core.ViewModelFactory;
import com.alvindizon.launcher.databinding.FragmentAppListBinding;
import com.alvindizon.launcher.di.Injector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class AppListFragment extends Fragment{
    private static final String TAG = AppListFragment.class.getSimpleName();
    private List<AppModel> appList = new ArrayList<>();
    private List<AppModel> faveAppList = new ArrayList<>();

    private AppListAdapter appListAdapter;
    private FragmentAppListBinding binding;
    private NavController navController;

    @Inject
    public ViewModelFactory viewModelFactory;

    private MainViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Injector.getViewModelComponent().inject(this);
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(MainViewModel.class);
        viewModel.setPackageManager(requireActivity().getPackageManager());
        navController = ((MainActivity) requireActivity()).getNavController();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAppListBinding.inflate(inflater, container, false);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
            new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    navController.navigateUp();
                }
            });

        appListAdapter = new AppListAdapter(new AppListAdapter.AppItemListener() {
            @Override
            public void onItemClick(AppModel app) {
                faveAppList.add(app);
            }

            @Override
            public void onItemUncheck(AppModel app) {
                faveAppList.remove(app);
            }
        });

        binding.rvNav.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNav.setAdapter(appListAdapter);

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

        binding.fab.setOnClickListener(v ->
                viewModel.saveToExistingFaves(faveAppList).observe(getViewLifecycleOwner(), this::handleSaveStatus));
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.getLaunchableApps().observe(this, data -> {
            appList = data;
            appListAdapter.setAppList(appList);
            binding.progressBar.setVisibility(View.GONE);
            binding.rvNav.setVisibility(View.VISIBLE);
        });
    }

    private void handleSaveStatus(SaveStatus saveStatus) {
        switch (saveStatus) {
            case SAVING:
                Log.d(TAG, "handleSaveStatus: saving");
                break;
            case DONE:
                Log.d(TAG, "handleSaveStatus: done");
                Toast.makeText(requireContext(), R.string.prompt_saved, Toast.LENGTH_SHORT).show();
                navController.navigateUp();
                break;
            case ERROR:
                Log.d(TAG, "handleSaveStatus: error");
                Toast.makeText(requireContext(), R.string.prompt_error, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }
}
