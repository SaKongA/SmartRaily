package com.sakongapps.smartraily.command;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.sakongapps.smartraily.R;

import java.util.List;

public class CommandAdapter extends RecyclerView.Adapter<CommandAdapter.CommandViewHolder> {
    
    private List<Command> commands;
    private final OnCommandClickListener listener;
    
    public CommandAdapter(List<Command> commands, OnCommandClickListener listener) {
        this.commands = commands;
        this.listener = listener;
    }
    
    @SuppressLint("NotifyDataSetChanged")
    public void updateCommands(List<Command> commands) {
        this.commands = commands;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public CommandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_command, parent, false);
        return new CommandViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CommandViewHolder holder, int position) {
        Command command = commands.get(position);
        holder.bind(command, listener);
    }
    
    @Override
    public int getItemCount() {
        return commands.size();
    }
    
    public static class CommandViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView nameText;
        private final TextView contentText;
        
        public CommandViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            nameText = itemView.findViewById(R.id.commandNameText);
            contentText = itemView.findViewById(R.id.commandContentText);
        }
        
        public void bind(Command command, OnCommandClickListener listener) {
            nameText.setText(command.getName());
            contentText.setText(command.getContent());
            
            cardView.setOnClickListener(v -> listener.onCommandClick(command));
            cardView.setOnLongClickListener(v -> {
                listener.onCommandLongClick(command);
                return true;
            });
        }
    }
    
    public interface OnCommandClickListener {
        void onCommandClick(Command command);
        void onCommandLongClick(Command command);
    }
} 