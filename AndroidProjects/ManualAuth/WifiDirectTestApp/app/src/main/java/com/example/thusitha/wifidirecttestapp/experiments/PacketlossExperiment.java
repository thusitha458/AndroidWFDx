package com.example.thusitha.wifidirecttestapp.experiments;


import com.example.thusitha.wifidirecttestapp.logging.FileLoggerFactory;
import com.example.thusitha.wifidirecttestapp.logging.LoggerType;

public class PacketLossExperiment extends PeriodicSender {

    private static PacketLossExperiment instance = new PacketLossExperiment();

    private PacketLossExperiment() {
    }

    public static PacketLossExperiment getInstance () {
        return instance;
    }

    @Override
    protected void setFileLogger () {
        fileLogger = (new FileLoggerFactory()).getFileLogger(LoggerType.LOGGER_1);
        fileLogger.createLogFile("pl-"
                .concat(String.valueOf(messageSize).concat("-"))
                .concat(String.valueOf(periodMS).concat("-"))
                .concat(String.valueOf(durationMS).concat("-"))
                .concat(String.valueOf(distanceM).concat("-"))
                .concat(isSender ? "s" : "r")
                .concat(".txt")
        );
    }

}
