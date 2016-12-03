package com.example.thusitha.wifidirecttestapp.experiments;


public enum ExperimentType {
    THROUGHPUT("Throughput"),
    PACKET_LOSS("Packet Loss"),
    PACKET_DELAY("Packet Delay");

    private final String name;

    ExperimentType (String name) {
        this.name = name;
    }

    @Override
    public String toString () {
        return name;
    }
}
