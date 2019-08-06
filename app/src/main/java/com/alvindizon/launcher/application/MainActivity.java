package com.alvindizon.launcher.application;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.alvindizon.launcher.R;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String FAVE_LIST = "key_fave_list";

    private NavController navController;
    private SharedPreferences sharedPreferences;
    JsonAdapter<List<String>> faveAppJsonAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        sharedPreferences = getSharedPreferences(getString(R.string.app_name), 0);
        Moshi moshi = new Moshi.Builder().build();

        faveAppJsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, String.class));
    }

    public NavController getNavController() {
        return navController;
    }

    public SharedPreferences getCustomSharedPrefs() {
        return sharedPreferences;
    }

    public JsonAdapter<List<String>> getFaveAppJsonAdapter() {
        return faveAppJsonAdapter;
    }
}
