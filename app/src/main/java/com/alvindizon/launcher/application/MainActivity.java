package com.alvindizon.launcher.application;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.alvindizon.launcher.R;
import com.alvindizon.launcher.core.ViewModelFactory;
import com.alvindizon.launcher.di.Injector;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private MainViewModel viewModel;

    @Inject
    ViewModelFactory viewModelFactory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Injector.getViewModelComponent().inject(this);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    }

    public NavController getNavController() {
        return navController;
    }
}
