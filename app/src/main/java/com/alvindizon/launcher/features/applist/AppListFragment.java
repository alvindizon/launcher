package com.alvindizon.launcher.features.applist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvindizon.launcher.core.AppModel;
import com.alvindizon.launcher.databinding.FragmentAppListBinding;
import com.alvindizon.launcher.features.main.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppListFragment extends Fragment{
    private List<AppModel> appList = new ArrayList<>();

    private AppListAdapter appListAdapter;
    private FragmentAppListBinding binding;

    private MainViewModel viewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAppListBinding.inflate(inflater, container, false);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
            new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    NavHostFragment.findNavController(AppListFragment.this).navigateUp();
                }
            });

        appListAdapter = new AppListAdapter(new AppListAdapter.AppItemListener() {
            @Override
            public void onItemClick(AppModel app) {
                // actual saving app to DB happens here, not on FAB click
                viewModel.addFaveApp(app);
            }

            @Override
            public void onItemUncheck(AppModel app) {}
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

        binding.fab.setOnClickListener(v -> NavHostFragment.findNavController(AppListFragment.this).navigateUp());
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
}
