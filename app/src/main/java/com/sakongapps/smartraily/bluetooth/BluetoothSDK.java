package com.sakongapps.smartraily.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BluetoothSDK {
    private static final String TAG = "BluetoothSDK";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int MAX_LOG_ENTRIES = 15;
    
    private static BluetoothSDK instance;
    private final BluetoothAdapter bluetoothAdapter;
    private final ExecutorService executor;
    private final Handler mainHandler;
    
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isConnected = false;
    private BluetoothDevice connectedDevice;
    
    private final List<BluetoothConnectionListener> listeners = new ArrayList<>();
    private final List<LogEntry> logEntries = Collections.synchronizedList(new ArrayList<>());
    
    // 单例模式
    public static synchronized BluetoothSDK getInstance() {
        if (instance == null) {
            instance = new BluetoothSDK();
        }
        return instance;
    }
    
    private BluetoothSDK() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    // 获取已配对设备列表
    public List<BluetoothDevice> getPairedDevices(Context context) {
        List<BluetoothDevice> deviceList = new ArrayList<>();
        
        if (bluetoothAdapter == null) {
            return deviceList;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                return deviceList;
            }
        }
        
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            deviceList.addAll(pairedDevices);
        }
        
        return deviceList;
    }
    
    // 连接到蓝牙设备
    public void connect(Context context, BluetoothDevice device) {
        if (isConnected) {
            disconnect();
        }
        
        executor.execute(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) 
                            != PackageManager.PERMISSION_GRANTED) {
                        notifyConnectionFailed("缺少BLUETOOTH_CONNECT权限");
                        return;
                    }
                }
                
                // 取消蓝牙发现，这可能会干扰连接
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                
                // 创建并连接socket
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                
                // 设置超时
                bluetoothSocket.connect();
                
                // 获取输入输出流
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
                
                // 测试连接是否正常
                if (outputStream != null) {
                    try {
                        // 发送一个测试字节，确认连接正常
                        outputStream.write(new byte[]{0});
                        outputStream.flush();
                    } catch (IOException e) {
                        Log.e(TAG, "连接测试失败: " + e.getMessage());
                        throw e; // 重新抛出异常，触发连接失败处理
                    }
                }
                
                isConnected = true;
                connectedDevice = device;
                
                notifyConnectionSuccess(device);
                
                // 启动读取线程
                startReadThread();
                
            } catch (IOException e) {
                Log.e(TAG, "连接失败: " + e.getMessage());
                notifyConnectionFailed(e.getMessage());
                closeConnection();
            }
        });
    }
    
    // 断开连接
    public void disconnect() {
        if (isConnected) {
            BluetoothDevice device = connectedDevice;
            closeConnection();
            notifyDisconnected(device);
        }
    }
    
    // 关闭连接
    private void closeConnection() {
        isConnected = false;
        
        try {
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "关闭连接失败: " + e.getMessage());
        }
    }
    
    // 发送数据
    public boolean sendData(String data) {
        if (!isConnected || outputStream == null) {
            Log.e(TAG, "发送失败：未连接或输出流为空");
            return false;
        }
        
        try {
            // 确保数据以换行符结尾，很多蓝牙设备需要这个
            if (!data.endsWith("\r\n")) {
                data = data + "\r\n";
            }
            
            final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            
            // 先添加发送日志，确保UI上能看到
            LogEntry logEntry = new LogEntry(LogEntry.DIRECTION_OUTGOING, bytes);
            addLogEntry(logEntry);
            
            // 同步发送数据，确保发送成功
            try {
                outputStream.write(bytes);
                outputStream.flush();
                Log.d(TAG, "数据发送成功: " + data.trim());
                return true;
            } catch (IOException e) {
                Log.e(TAG, "发送数据失败: " + e.getMessage());
                mainHandler.post(() -> {
                    for (BluetoothConnectionListener listener : listeners) {
                        listener.onConnectionFailed("发送数据失败: " + e.getMessage());
                    }
                });
                disconnect();
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "准备发送数据时出错: " + e.getMessage());
            return false;
        }
    }
    
    // 启动读取线程
    private void startReadThread() {
        executor.execute(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            
            while (isConnected) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        byte[] data = new byte[bytes];
                        System.arraycopy(buffer, 0, data, 0, bytes);
                        
                        // 添加接收日志 - 只在这里添加，不在notifyDataReceived中重复添加
                        addLogEntry(new LogEntry(LogEntry.DIRECTION_INCOMING, data));
                        
                        // 通知数据接收，但不再创建新的日志条目
                        notifyDataReceived(data);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "读取数据失败: " + e.getMessage());
                    disconnect();
                    break;
                }
            }
        });
    }
    
    // 添加日志条目
    private void addLogEntry(LogEntry entry) {
        Log.d(TAG, "添加日志: " + (entry.getDirection() == LogEntry.DIRECTION_INCOMING ? "接收" : "发送") + 
              " 内容: " + entry.getTextString());
        
        synchronized (logEntries) {
            logEntries.add(0, entry);
            while (logEntries.size() > MAX_LOG_ENTRIES) {
                logEntries.remove(logEntries.size() - 1);
            }
        }
        
        mainHandler.post(() -> {
            for (BluetoothConnectionListener listener : listeners) {
                if (listener instanceof LogListener) {
                    ((LogListener) listener).onLogEntryAdded(entry);
                }
            }
        });
    }
    
    // 获取所有日志条目
    public List<LogEntry> getLogEntries() {
        synchronized (logEntries) {
            return new ArrayList<>(logEntries);
        }
    }
    
    // 清除所有日志
    public void clearLogs() {
        synchronized (logEntries) {
            logEntries.clear();
        }
        
        mainHandler.post(() -> {
            for (BluetoothConnectionListener listener : listeners) {
                if (listener instanceof LogListener) {
                    ((LogListener) listener).onLogCleared();
                }
            }
        });
    }
    
    // 添加监听器
    public void addListener(BluetoothConnectionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    // 移除监听器
    public void removeListener(BluetoothConnectionListener listener) {
        listeners.remove(listener);
    }
    
    // 通知连接成功
    private void notifyConnectionSuccess(BluetoothDevice device) {
        mainHandler.post(() -> {
            for (BluetoothConnectionListener listener : listeners) {
                listener.onConnected(device);
            }
        });
    }
    
    // 通知连接失败
    private void notifyConnectionFailed(String message) {
        mainHandler.post(() -> {
            for (BluetoothConnectionListener listener : listeners) {
                listener.onConnectionFailed(message);
            }
        });
    }
    
    // 通知断开连接
    private void notifyDisconnected(BluetoothDevice device) {
        mainHandler.post(() -> {
            for (BluetoothConnectionListener listener : listeners) {
                listener.onDisconnected(device);
            }
        });
    }
    
    // 通知数据接收
    private void notifyDataReceived(byte[] data) {
        mainHandler.post(() -> {
            for (BluetoothConnectionListener listener : listeners) {
                listener.onDataReceived(data);
            }
        });
    }
    
    // 获取连接状态
    public boolean isConnected() {
        return isConnected;
    }
    
    // 获取已连接设备
    public BluetoothDevice getConnectedDevice() {
        return connectedDevice;
    }
    
    // 蓝牙连接监听器接口
    public interface BluetoothConnectionListener {
        void onConnected(BluetoothDevice device);
        void onDisconnected(BluetoothDevice device);
        void onConnectionFailed(String message);
        void onDataReceived(byte[] data);
    }
    
    // 日志监听器接口
    public interface LogListener extends BluetoothConnectionListener {
        void onLogEntryAdded(LogEntry entry);
        void onLogCleared();
    }
} 