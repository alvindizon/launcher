package com.alvindizon.launcher.features.faveapps;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.application.MainActivity;
import com.alvindizon.launcher.core.AppModel;
import com.alvindizon.launcher.core.SaveStatus;
import com.alvindizon.launcher.core.ViewModelFactory;
import com.alvindizon.launcher.databinding.FragmentFavoritesBinding;
import com.alvindizon.launcher.di.Injector;
import com.squareup.moshi.JsonAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class FavoritesFragment extends Fragment {
    public static final String TAG = FavoritesFragment.class.getSimpleName();

    private PackageManager packageManager;
    private FaveListAdapter faveListAdapter;
    FragmentFavoritesBinding binding;
    private List<AppModel> faveList = new ArrayList<>();
    private List<String> favePackageNameList = new ArrayList<>();
    private NavController navController;
    private SharedPreferences preferences;
    JsonAdapter<List<String>> faveAppJsonAdapter;

    @Inject
    public ViewModelFactory viewModelFactory;

    private FavoritesViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Injector.getViewModelComponent().inject(this);
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FavoritesViewModel.class);
        viewModel.setPackageManager(requireActivity().getPackageManager());
        navController = ((MainActivity) requireActivity()).getNavController();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
            new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // do nothing on back press
                }
            });

        binding.button.setOnClickListener((v ->
                navController.navigate(R.id.action_favorites_dest_to_app_list_dest)));
        binding.toolbar.inflateMenu(R.menu.menu_add_apps);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_disp_add_apps:
                    navController.navigate(R.id.action_favorites_dest_to_app_list_dest);
                    return true;
                default:
                    break;
            }
            return super.onOptionsItemSelected(item);
        });

        faveListAdapter = new FaveListAdapter(this::launchApp);
        faveListAdapter.setAppList(faveList);

        faveListAdapter.setOnDeleteItemListener(list -> {
            faveList = list;
            viewModel.saveFaveApps(faveList).observe(getViewLifecycleOwner(), this::handleSaveStatus);
            updateRecyclerView();
        });

        DividerItemDecoration itemDecorator = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(requireContext(), R.drawable.recycler_horizontal_bottom_border)));
        binding.rvNav.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNav.addItemDecoration(itemDecorator);
        binding.rvNav.setAdapter(faveListAdapter);
        Log.d(TAG, "onCreateView: done initial RV setup");
        updateRecyclerView();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        faveListAdapter.clear();
        viewModel.loadFaveAppList().observe(this, list -> {
            faveList = new ArrayList<>(list);
            faveListAdapter.setAppList(faveList);
            updateRecyclerView();
        });
    }

    private void updateRecyclerView() {
        Log.d(TAG, "updateRecyclerView: start");
        if(faveList.isEmpty()) {
            binding.button.setVisibility(View.VISIBLE);
            binding.frameFav.setVisibility(View.GONE);
        } else {
            binding.button.setVisibility(View.GONE);
            binding.frameFav.setVisibility(View.VISIBLE);
        }
    }

    private void launchApp(String packageName) {
        try {
            startActivity(packageManager.getLaunchIntentForPackage(packageName));
        } catch (Exception e) {
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
}
