package com.example.thusitha.wifidirecttestapp;


public class FileLoggerCreator {

    public enum Experiments {
        EXPERIMENT_1
    }

    public FileLoggerCreator () {}

    public FileLogger getFileLogger (Experiments experiment) {

        switch (experiment) {
            case EXPERIMENT_1:
                return LoggerExp1.getInstance();
            default:
                return null;
        }
    }

}
