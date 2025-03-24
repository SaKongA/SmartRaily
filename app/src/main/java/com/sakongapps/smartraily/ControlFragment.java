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

public class ControlFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 获取两个卡片视图
        MaterialCardView remoteControlCard = view.findViewById(R.id.remoteControlCard);
        MaterialCardView cooperativeControlCard = view.findViewById(R.id.cooperativeControlCard);
        
        // 设置远程操控模式卡片点击事件
        remoteControlCard.setOnClickListener(v -> {
            // 这里可以导航到远程操控模式界面或执行相关操作
            Toast.makeText(requireContext(), "进入远程操控模式", Toast.LENGTH_SHORT).show();
        });
        
        // 设置协同操控模式卡片点击事件
        cooperativeControlCard.setOnClickListener(v -> {
            // 这里可以导航到协同操控模式界面或执行相关操作
            Toast.makeText(requireContext(), "进入协同操控模式", Toast.LENGTH_SHORT).show();
        });
    }
} 