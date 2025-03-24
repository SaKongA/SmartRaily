package com.sakongapps.smartraily.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sakongapps.smartraily.R;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private final List<BluetoothDevice> devices;
    private final OnDeviceClickListener listener;

    public DeviceAdapter(List<BluetoothDevice> devices, OnDeviceClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bluetooth_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        holder.bind(device, listener);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final TextView deviceNameText;
        private final TextView deviceAddressText;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameText = itemView.findViewById(R.id.deviceNameText);
            deviceAddressText = itemView.findViewById(R.id.deviceAddressText);
        }

        public void bind(BluetoothDevice device, OnDeviceClickListener listener) {
            String deviceName = "未知设备";
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(itemView.getContext(), 
                        Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    deviceName = device.getName() != null ? device.getName() : "未知设备";
                }
            } else {
                deviceName = device.getName() != null ? device.getName() : "未知设备";
            }
            
            deviceNameText.setText(deviceName);
            deviceAddressText.setText(device.getAddress());

            itemView.setOnClickListener(v -> listener.onDeviceClick(device));
        }
    }

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }
} 