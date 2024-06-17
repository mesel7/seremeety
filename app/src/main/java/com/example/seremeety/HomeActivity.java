package com.example.seremeety;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;

import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.seremeety.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_matching, R.id.navigation_request, R.id.navigation_chatlist, R.id.navigation_mypage)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    /*
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home);

        Map<String, Integer> fragmentMap = new HashMap<>();
        fragmentMap.put("openMatchingFragment", R.id.navigation_matching);
        fragmentMap.put("openRequestFragment", R.id.navigation_request);
        fragmentMap.put("openChatListFragment", R.id.navigation_chatlist);
        fragmentMap.put("openMyPageFragment", R.id.navigation_mypage);

        for (Map.Entry<String, Integer> entry : fragmentMap.entrySet()) {
            if (sharedPreferences.getBoolean(entry.getKey(), false)) {
                navController.popBackStack();
                navController.navigate(entry.getValue());
                binding.navView.setSelectedItemId(entry.getValue());

                sharedPreferences.edit().putBoolean(entry.getKey(), false).apply();
                break;
            }
        }
    }
    */
}

