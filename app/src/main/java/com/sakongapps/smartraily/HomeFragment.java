package com.sakongapps.smartraily;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sakongapps.smartraily.bluetooth.BluetoothSDK;
import com.sakongapps.smartraily.bluetooth.DeviceAdapter;
import com.sakongapps.smartraily.bluetooth.LogAdapter;
import com.sakongapps.smartraily.bluetooth.LogEntry;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements BluetoothSDK.LogListener {

    private TextView connectionStatusText;
    private TextView deviceInfoText;
    private RecyclerView logRecyclerView;

    private BluetoothSDK bluetoothSDK;
    private LogAdapter logAdapter;
    private final List<LogEntry> logEntries = new ArrayList<>();
    private final List<String> permissionsNeeded = new ArrayList<>();
    
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                for (Boolean isGranted : permissions.values()) {
                    allGranted = allGranted && isGranted;
                }
                
                if (allGranted) {
                    showDeviceSelectionDialog();
                } else {
                    Toast.makeText(requireContext(), R.string.permission_required, Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化视图
        MaterialCardView connectionStatusCard = view.findViewById(R.id.connectionStatusCard);
        connectionStatusText = view.findViewById(R.id.connectionStatusText);
        deviceInfoText = view.findViewById(R.id.deviceInfoText);
        logRecyclerView = view.findViewById(R.id.logRecyclerView);
        MaterialButton clearLogButton = view.findViewById(R.id.clearLogButton);
        
        // 获取蓝牙SDK实例
        bluetoothSDK = BluetoothSDK.getInstance();
        
        // 设置卡片点击事件
        connectionStatusCard.setOnClickListener(v -> {
            if (!bluetoothSDK.isConnected()) {
                checkAndRequestPermissions();
            } else {
                showDisconnectConfirmDialog();
            }
        });
        
        // 设置日志RecyclerView
        logRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        logAdapter = new LogAdapter(logEntries);
        logRecyclerView.setAdapter(logAdapter);
        
        // 设置清除日志按钮
        clearLogButton.setOnClickListener(v -> {
            bluetoothSDK.clearLogs(); // 使用SDK的方法清除日志
        });
        
        // 更新UI显示
        updateConnectionStatus();
        
        // 加载现有日志
        loadExistingLogs();
    }
    
    // 加载现有日志
    @SuppressLint("NotifyDataSetChanged")
    private void loadExistingLogs() {
        List<LogEntry> existingLogs = bluetoothSDK.getLogEntries();
        if (!existingLogs.isEmpty()) {
            logEntries.clear();
            logEntries.addAll(existingLogs);
            logAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bluetoothSDK.addListener(this);
        updateConnectionStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        bluetoothSDK.removeListener(this);
    }
    
    // 检查并请求权限
    private void checkAndRequestPermissions() {
        permissionsNeeded.clear();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
        
        if (!permissionsNeeded.isEmpty()) {
            requestPermissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        } else {
            showDeviceSelectionDialog();
        }
    }
    
    // 显示设备选择对话框
    private void showDeviceSelectionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_device_selection, null);
        RecyclerView deviceRecyclerView = dialogView.findViewById(R.id.deviceRecyclerView);
        View emptyView = dialogView.findViewById(R.id.emptyView);
        
        List<BluetoothDevice> pairedDevices = bluetoothSDK.getPairedDevices(requireContext());
        
        if (pairedDevices.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            deviceRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            deviceRecyclerView.setVisibility(View.VISIBLE);
            
            deviceRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            DeviceAdapter adapter = new DeviceAdapter(pairedDevices, this::connectToDevice);
            deviceRecyclerView.setAdapter(adapter);
        }
        
        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    // 显示断开连接确认对话框
    private void showDisconnectConfirmDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.disconnect)
                .setMessage(R.string.disconnect_confirm)
                .setPositiveButton(R.string.confirm, (dialog, which) -> bluetoothSDK.disconnect())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    // 连接到设备
    private void connectToDevice(BluetoothDevice device) {
        Toast.makeText(requireContext(), R.string.connecting, Toast.LENGTH_SHORT).show();
        bluetoothSDK.connect(requireContext(), device);
    }
    
    // 更新连接状态UI
    private void updateConnectionStatus() {
        if (bluetoothSDK.isConnected()) {
            BluetoothDevice device = bluetoothSDK.getConnectedDevice();
            connectionStatusText.setText(R.string.connected);
            
            String deviceName = getString(R.string.unknown_device);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) 
                        == PackageManager.PERMISSION_GRANTED && device.getName() != null) {
                    deviceName = device.getName();
                }
            } else if (device.getName() != null) {
                deviceName = device.getName();
            }
            
            deviceInfoText.setText(String.format("%s (%s)", deviceName, device.getAddress()));
            deviceInfoText.setVisibility(View.VISIBLE);
        } else {
            connectionStatusText.setText(R.string.not_connected);
            deviceInfoText.setVisibility(View.GONE);
        }
    }

    // BluetoothConnectionListener 接口实现
    @Override
    public void onConnected(BluetoothDevice device) {
        updateConnectionStatus();
        Toast.makeText(requireContext(), R.string.connection_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected(BluetoothDevice device) {
        updateConnectionStatus();
        Toast.makeText(requireContext(), R.string.disconnected, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(String message) {
        updateConnectionStatus();
        Toast.makeText(requireContext(), getString(R.string.connection_failed) + ": " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataReceived(byte[] data) {
        // 不需要在这里创建日志条目，因为BluetoothSDK已经在接收数据时创建了
        // 避免重复创建日志
    }
    
    @Override
    public void onLogEntryAdded(LogEntry entry) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                // 确保不重复添加
                boolean alreadyExists = false;
                for (LogEntry existingEntry : logEntries) {
                    if (existingEntry == entry) {
                        alreadyExists = true;
                        break;
                    }
                }
                
                if (!alreadyExists) {
                    logEntries.add(0, entry);
                    logAdapter.notifyItemInserted(0);
                    // 滚动到顶部显示最新日志
                    logRecyclerView.scrollToPosition(0);
                }
            });
        }
    }
    
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onLogCleared() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                logEntries.clear();
                logAdapter.notifyDataSetChanged();
            });
        }
    }
} 