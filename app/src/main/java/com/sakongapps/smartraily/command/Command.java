package com.sakongapps.smartraily.command;

import java.util.UUID;

public class Command {
    private final String id;
    private String name;
    private String content;
    
    public Command(String name, String content) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.content = content;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
} 