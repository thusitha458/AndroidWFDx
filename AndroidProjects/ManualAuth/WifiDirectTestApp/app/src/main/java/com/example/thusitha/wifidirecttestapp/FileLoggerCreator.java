package com.example.thusitha.wifidirecttestapp;


public class FileLoggerCreator {

    public FileLoggerCreator () {}

    public FileLogger getFileLogger (ExperimentFactory.Experiments experiment) {

        switch (experiment) {
            case EXPERIMENT_1:
                return LoggerExp1.getInstance();
            default:
                return null;
        }
    }

}
