package com.alvindizon.launcher.features.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.core.PreferenceHelper;
import com.alvindizon.launcher.core.ViewModelFactory;
import com.alvindizon.launcher.databinding.ActivityMainBinding;
import com.alvindizon.launcher.di.Injector;
import com.alvindizon.launcher.di.module.ActivityModule;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private MainViewModel viewModel;
    private ActivityMainBinding binding; // TODO assign binding

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Injector.get().activityComponent(new ActivityModule(this)).inject(this);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // determine whether to start using list or grid fragment
        // by obtaining last used orientation from shared prefs
        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.navigation_main);
        if(preferenceHelper.get(R.string.key_is_list, true)) {
            navGraph.setStartDestination(R.id.vertical_list_dest);
        } else {
            navGraph.setStartDestination(R.id.grid_list_dest);
        }
        navController.setGraph(navGraph);
    }

    @Override
    protected void onStart() {
        super.onStart();
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(destination.getId() == R.id.app_list_dest) {
                binding.toolbarTitle.setText(R.string.label_add_fave_apps);
            } else {
                binding.toolbarTitle.setText(R.string.label_fave_apps);
            }
        });

    }

    public NavController getNavController() {
        return navController;
    }
}
