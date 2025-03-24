package com.sakongapps.smartraily;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.sakongapps.smartraily.bluetooth.BluetoothSDK;
import com.sakongapps.smartraily.command.Command;
import com.sakongapps.smartraily.command.CommandAdapter;
import com.sakongapps.smartraily.command.CommandManager;

import java.util.List;

public class DebugFragment extends Fragment implements CommandManager.CommandListener {

    private RecyclerView commandRecyclerView;
    private TextView emptyView;
    private CommandAdapter commandAdapter;
    private CommandManager commandManager;
    private BluetoothSDK bluetoothSDK;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_debug, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 设置菜单
        setupMenu();
        
        commandRecyclerView = view.findViewById(R.id.commandRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        
        // 获取命令管理器和蓝牙SDK实例
        commandManager = CommandManager.getInstance(requireContext());
        bluetoothSDK = BluetoothSDK.getInstance();
        
        // 设置网格布局，一行两个
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        commandRecyclerView.setLayoutManager(layoutManager);
        
        // 初始化适配器
        List<Command> commands = commandManager.getCommands();
        commandAdapter = new CommandAdapter(commands, new CommandAdapter.OnCommandClickListener() {
            @Override
            public void onCommandClick(Command command) {
                sendCommand(command);
            }

            @Override
            public void onCommandLongClick(Command command) {
                showCommandOptionsDialog(command);
            }
        });
        
        commandRecyclerView.setAdapter(commandAdapter);
        
        // 更新空视图状态
        updateEmptyViewVisibility();

        Button testConnectionButton = view.findViewById(R.id.testConnectionButton);
        testConnectionButton.setOnClickListener(v -> {
            if (bluetoothSDK.isConnected()) {
                // 发送一个简单的测试字符串
                boolean success = bluetoothSDK.sendData("TEST");
                Toast.makeText(requireContext(), 
                        success ? "测试数据已发送" : "发送测试数据失败", 
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "请先连接蓝牙设备", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // 设置菜单
    private void setupMenu() {
        // 获取MenuHost
        MenuHost menuHost = requireActivity();
        
        // 添加MenuProvider
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.debug_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_add_command) {
                    showAddCommandDialog(null);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onResume() {
        super.onResume();
        commandManager.addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        commandManager.removeListener(this);
    }

    @Override
    public void onCommandsChanged() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                commandAdapter.updateCommands(commandManager.getCommands());
                updateEmptyViewVisibility();
            });
        }
    }

    private void updateEmptyViewVisibility() {
        if (commandManager.getCommands().isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            commandRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            commandRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddCommandDialog(Command existingCommand) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_command, null);
        
        TextInputEditText nameInput = dialogView.findViewById(R.id.commandNameInput);
        TextInputEditText contentInput = dialogView.findViewById(R.id.commandContentInput);
        
        // 如果是编辑现有命令，填充现有数据
        if (existingCommand != null) {
            nameInput.setText(existingCommand.getName());
            contentInput.setText(existingCommand.getContent());
        }
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existingCommand == null ? R.string.add_command : R.string.edit_command)
                .setView(dialogView)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
                    String content = contentInput.getText() != null ? contentInput.getText().toString().trim() : "";
                    
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.command_name_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (content.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.command_content_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (existingCommand == null) {
                        // 添加新命令
                        Command newCommand = new Command(name, content);
                        commandManager.addCommand(newCommand);
                    } else {
                        // 更新现有命令
                        existingCommand.setName(name);
                        existingCommand.setContent(content);
                        commandManager.updateCommand(existingCommand);
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        
        builder.show();
    }

    private void showCommandOptionsDialog(Command command) {
        new MaterialAlertDialogBuilder(requireContext())
                .setItems(new CharSequence[]{
                        getString(R.string.edit_command),
                        getString(R.string.delete_command)
                }, (dialog, which) -> {
                    switch (which) {
                        case 0: // 编辑
                            showAddCommandDialog(command);
                            break;
                        case 1: // 删除
                            showDeleteConfirmDialog(command);
                            break;
                    }
                })
                .show();
    }

    private void showDeleteConfirmDialog(Command command) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_command)
                .setMessage(getString(R.string.delete_command) + ": " + command.getName())
                .setPositiveButton(R.string.delete, (dialog, which) -> commandManager.deleteCommand(command.getId()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void sendCommand(Command command) {
        if (bluetoothSDK.isConnected()) {
            String content = command.getContent();
            // 确保内容不为空
            if (content == null || content.isEmpty()) {
                Toast.makeText(requireContext(), "指令内容为空", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Log.d("DebugFragment", "发送指令: " + content);
            
            // 发送指令
            boolean success = bluetoothSDK.sendData(content);
            
            if (success) {
                Toast.makeText(requireContext(), R.string.command_sent, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.command_send_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "请先连接蓝牙设备", Toast.LENGTH_SHORT).show();
        }
    }
} 