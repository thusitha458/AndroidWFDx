package com.example.thusitha.wifidirecttestapp.logging;

public interface FileLogger {

    void createLogFile();
    void createLogFile(String fileName);
    void appendLog(String text);
    String getFileName();

}
