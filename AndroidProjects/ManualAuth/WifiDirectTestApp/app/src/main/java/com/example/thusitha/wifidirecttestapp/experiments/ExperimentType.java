package com.example.thusitha.wifidirecttestapp.experiments;


public enum ExperimentType {
    EXPERIMENT_1("Experiment1");

    private final String name;

    ExperimentType (String name) {
        this.name = name;
    }

    @Override
    public String toString () {
        return name;
    }
}
