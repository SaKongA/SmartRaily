package com.sakongapps.smartraily.bluetooth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sakongapps.smartraily.R;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    
    private final List<LogEntry> logEntries;
    
    public LogAdapter(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }
    
    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log_entry, parent, false);
        return new LogViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry entry = logEntries.get(position);
        holder.bind(entry);
    }
    
    @Override
    public int getItemCount() {
        return logEntries.size();
    }
    
    public static class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeText;
        private final TextView directionText;
        private final TextView dataText;
        
        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.timeText);
            directionText = itemView.findViewById(R.id.directionText);
            dataText = itemView.findViewById(R.id.dataText);
        }
        
        public void bind(LogEntry entry) {
            timeText.setText(entry.getFormattedTime());
            
            String direction = entry.getDirectionString();
            directionText.setText(direction);
            
            // 设置方向文本颜色
            int colorRes = entry.getDirection() == LogEntry.DIRECTION_INCOMING ? 
                    R.color.log_incoming : R.color.log_outgoing;
            directionText.setTextColor(itemView.getContext().getColor(colorRes));
            
            // 显示文本数据而不是十六进制
            dataText.setText(entry.getTextString());
        }
    }
} 