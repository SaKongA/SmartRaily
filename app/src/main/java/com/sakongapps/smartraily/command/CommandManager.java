package com.sakongapps.smartraily.command;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private static final String TAG = "CommandManager";
    private static final String PREF_NAME = "commands_prefs";
    private static final String KEY_COMMANDS = "commands";
    
    private static CommandManager instance;
    private final SharedPreferences preferences;
    private final Gson gson;
    private List<Command> commands;
    private final List<CommandListener> listeners = new ArrayList<>();
    
    public interface CommandListener {
        void onCommandsChanged();
    }
    
    private CommandManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadCommands();
    }
    
    public static synchronized CommandManager getInstance(Context context) {
        if (instance == null) {
            instance = new CommandManager(context);
        }
        return instance;
    }
    
    private void loadCommands() {
        String json = preferences.getString(KEY_COMMANDS, null);
        if (json != null) {
            try {
                Type type = new TypeToken<List<Command>>(){}.getType();
                commands = gson.fromJson(json, type);
            } catch (Exception e) {
                Log.e(TAG, "Error loading commands: " + e.getMessage());
                commands = new ArrayList<>();
            }
        } else {
            commands = new ArrayList<>();
        }
    }
    
    private void saveCommands() {
        try {
            String json = gson.toJson(commands);
            preferences.edit().putString(KEY_COMMANDS, json).apply();
            notifyListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error saving commands: " + e.getMessage());
        }
    }
    
    public List<Command> getCommands() {
        return new ArrayList<>(commands);
    }
    
    public void addCommand(Command command) {
        commands.add(command);
        saveCommands();
    }
    
    public void updateCommand(Command command) {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).getId().equals(command.getId())) {
                commands.set(i, command);
                saveCommands();
                return;
            }
        }
    }
    
    public void deleteCommand(String commandId) {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).getId().equals(commandId)) {
                commands.remove(i);
                saveCommands();
                return;
            }
        }
    }
    
    public void addListener(CommandListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(CommandListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners() {
        for (CommandListener listener : listeners) {
            listener.onCommandsChanged();
        }
    }
} 