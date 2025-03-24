package com.sakongapps.smartraily;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

public class AutoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auto, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 获取两个卡片视图
        MaterialCardView regularInspectionCard = view.findViewById(R.id.regularInspectionCard);
        MaterialCardView emergencyInspectionCard = view.findViewById(R.id.emergencyInspectionCard);
        
        // 设置常规巡检模式卡片点击事件
        regularInspectionCard.setOnClickListener(v -> {
            // 这里可以导航到常规巡检模式界面或执行相关操作
            Toast.makeText(requireContext(), "进入常规巡检模式", Toast.LENGTH_SHORT).show();
        });
        
        // 设置应急巡检模式卡片点击事件
        emergencyInspectionCard.setOnClickListener(v -> {
            // 这里可以导航到应急巡检模式界面或执行相关操作
            Toast.makeText(requireContext(), "进入应急巡检模式", Toast.LENGTH_SHORT).show();
        });
    }
} 