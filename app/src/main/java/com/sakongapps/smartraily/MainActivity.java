package com.sakongapps.smartraily;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 启用边缘到边缘显示
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_main);

        // 设置顶部工具栏
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        
        // 设置AppBarLayout的内边距以适应状态栏
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (v, windowInsets) -> {
            int topInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            appBarLayout.setPadding(0, topInset, 0, 0);
            return WindowInsetsCompat.CONSUMED;
        });

        // 设置底部导航
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        
        // 正确获取NavHostFragment和NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        
        // 配置顶部应用栏，设置哪些目的地被视为顶级目的地
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.manualFragment, R.id.autoFragment, R.id.settingsFragment
        ).build();
        
        // 将顶部应用栏与导航控制器连接
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        
        // 将底部导航与导航控制器连接
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) 
                || super.onSupportNavigateUp();
    }
} 