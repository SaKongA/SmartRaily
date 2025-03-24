package com.sakongapps.smartraily;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化调试模式选项
        LinearLayout debugModeOption = view.findViewById(R.id.debugModeOption);

        // 设置点击事件
        debugModeOption.setOnClickListener(v -> {
            // 导航到调试页面
            navigateToDebugFragment();
        });
        
        // 初始化关于APP选项
        LinearLayout aboutAppOption = view.findViewById(R.id.aboutAppOption);
        
        // 设置点击事件
        aboutAppOption.setOnClickListener(v -> {
            // 显示关于APP对话框
            showAboutAppDialog();
        });
    }

    // 导航到DebugFragment
    private void navigateToDebugFragment() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.debugFragment);
    }
    
    // 显示关于APP对话框
    private void showAboutAppDialog() {
        String appVersion = getAppVersion();
        
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_about_app, null);
        
        // 设置版本号
        TextView versionText = dialogView.findViewById(R.id.appVersionText);
        versionText.setText(getString(R.string.app_version, appVersion));
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.about_app)
                .setView(dialogView)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }
    
    // 获取应用版本号
    private String getAppVersion() {
        try {
            PackageInfo packageInfo = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0.0";
        }
    }
} 