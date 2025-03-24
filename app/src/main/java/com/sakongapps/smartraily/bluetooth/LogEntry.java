package com.sakongapps.smartraily.bluetooth;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogEntry {
    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;
    
    private final long timestamp;
    private final int direction;

    private final String textString;
    
    public LogEntry(int direction, byte[] data) {
        this.timestamp = System.currentTimeMillis();
        this.direction = direction;
        this.textString = bytesToText(data);
    }
    
    public int getDirection() {
        return direction;
    }
    
    public String getTextString() {
        return textString;
    }
    
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    public String getDirectionString() {
        return direction == DIRECTION_INCOMING ? "接收" : "发送";
    }

    private String bytesToText(byte[] bytes) {
        try {
            // 尝试将字节数组转换为UTF-8文本
            String text = new String(bytes, StandardCharsets.UTF_8);
            
            // 替换不可打印字符为点号
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c >= 32 && c < 127) {
                    sb.append(c);
                } else if (c == '\r') {
                    sb.append("\\r");
                } else if (c == '\n') {
                    sb.append("\\n");
                } else if (c == '\t') {
                    sb.append("\\t");
                } else {
                    sb.append('.');
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "[无法解析为文本]";
        }
    }
} 