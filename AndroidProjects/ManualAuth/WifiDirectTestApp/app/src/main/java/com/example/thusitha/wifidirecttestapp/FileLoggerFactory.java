package com.example.thusitha.wifidirecttestapp;


public class FileLoggerFactory {

    public enum LoggerType {
        LOGGER_1
    }

    public FileLoggerFactory() {}

    public FileLogger getFileLogger (LoggerType experiment) {

        switch (experiment) {
            case LOGGER_1:
                return Logger1.getInstance();
            default:
                return null;
        }
    }

}
