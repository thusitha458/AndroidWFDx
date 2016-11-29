package com.example.thusitha.wifidirecttestapp;

public interface FileLogger {

    void createLogFile();
    void createLogFile(String fileName);
    void appendLog(String text);
    String getFileName();

}
