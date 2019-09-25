package com.alvindizon.launcher.application;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.core.ViewModelFactory;
import com.alvindizon.launcher.databinding.ActivityMainBinding;
import com.alvindizon.launcher.di.Injector;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private MainViewModel viewModel;
    private ActivityMainBinding binding; // TODO assign binding

    @Inject
    ViewModelFactory viewModelFactory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Injector.getViewModelComponent().inject(this);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(destination.getId() == R.id.favorites_dest) {
                binding.toolbarTitle.setText(R.string.label_fave_apps);
            } else if(destination.getId() == R.id.app_list_dest) {
                binding.toolbarTitle.setText(R.string.label_add_fave_apps);
            }
        });
    }

    public NavController getNavController() {
        return navController;
    }
}
